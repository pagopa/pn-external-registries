package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.MessageContent;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.ThirdPartyData;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.db.io.dao.IOMessagesDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.IOMessagesEntity;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import it.pagopa.pn.external.registries.services.NotificationService;
import it.pagopa.pn.external.registries.services.TimelineService;
import it.pagopa.pn.external.registries.services.bottomsheet.BottomSheetContext;
import it.pagopa.pn.external.registries.services.bottomsheet.BottomSheetProcessorFactory;
import it.pagopa.pn.external.registries.services.bottomsheet.ExtendedDeliveryMode;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import it.pagopa.pn.external.registries.services.io.dto.UserStatusResponseInternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_NOTIFICATION_RECIPIENT_ID_NOT_FOUND;
import static it.pagopa.pn.external.registries.util.AppIOUtils.SENDER_DENOMINATION_PLACEHOLDER;
import static java.util.stream.IntStream.range;

@Service
@Slf4j
@RequiredArgsConstructor
public class IOService {

    private static final String IO_LOCALE_IT_IT = "it_IT";
    private final IOCourtesyMessageClient courtesyMessageClient;
    private final IOOptInClient optInClient;

    private final PnExternalRegistriesConfig cfg;
    private final SendIOSentMessageService sendIOSentMessageService;

    private final IOMessagesDao ioMessagesDao;

    private final BottomSheetProcessorFactory bottomSheetProcessorFactory;
    private final NotificationService notificationService;
    private final TimelineService timelineService;


    public Mono<SendMessageResponseDto> sendIOMessage( Mono<SendMessageRequestDto> sendMessageRequestDtoMono )
    {
        return sendMessageRequestDtoMono
                .flatMap(requestDto -> {
                    MDC.put(MDCUtils.MDC_PN_IUN_KEY, requestDto.getIun());
                    MDC.put(MDCUtils.MDC_CX_ID_KEY, requestDto.getRecipientInternalID());
                    if(requestDto.getRecipientIndex() != null) MDC.put(MDCUtils.MDC_PN_CTX_RECIPIENT_INDEX, requestDto.getRecipientIndex().toString());
                    MDC.put(MDCUtils.MDC_PN_CTX_TOPIC, "sendIOMessage");

                    log.info("[enter] sendIoMessage taxId={} iun={}", LogUtils.maskTaxId(requestDto.getRecipientTaxID()), requestDto.getIun());
                    return MDCUtils.addMDCToContextAndExecute(Mono.just(requestDto)
                            .zipWhen(sendMessageRequestDto -> {
                                        UserStatusRequestDto userStatusRequestDto = new UserStatusRequestDto().taxId(sendMessageRequestDto.getRecipientTaxID());
                                        return this.getUserStatusInternal( Mono.just(userStatusRequestDto) );
                                    },
                                    (sendMessageRequestDto0, responseStatusDto0) -> new Object(){
                                        public final UserStatusResponseInternal responseStatusDto = responseStatusDto0;
                                        public final SendMessageRequestDto sendMessageRequestDto = sendMessageRequestDto0;
                                    })
                            .flatMap(res -> {
                                UserStatusResponseInternal.StatusEnum ioStatus = res.responseStatusDto.getStatus();

                                switch (ioStatus){
                                    case APPIO_NOT_ACTIVE:
                                        log.info("IO is not available for user, not sending message taxId={} iun={}", LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID()), res.sendMessageRequestDto.getIun());
                                        SendMessageResponseDto resAppIoUnavailable = new SendMessageResponseDto();
                                        resAppIoUnavailable.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_APPIO_UNAVAILABLE);
                                        return Mono.just(resAppIoUnavailable);
                                    case PN_ACTIVE:
                                        return sendIOCourtesyMessage(res.sendMessageRequestDto, isPreferredLanguageIT(res.responseStatusDto.getPreferredLanguages()));
                                    case PN_NOT_ACTIVE:
                                        return manageOptIn(res.sendMessageRequestDto);
                                    case ERROR:
                                        log.info("Error in get user status, not sending message taxId={} iun={}", LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID()), res.sendMessageRequestDto.getIun());
                                        SendMessageResponseDto resErrorUserStatus = new SendMessageResponseDto();
                                        resErrorUserStatus.setResult(SendMessageResponseDto.ResultEnum.ERROR_USER_STATUS);
                                        return Mono.just(resErrorUserStatus);
                                    default:
                                        log.error(" ioStatus={} is not handled - iun={} taxId={}", ioStatus,  res.sendMessageRequestDto.getIun(), LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID()));
                                        return Mono.error(new PnInternalException("ioStatus="+ioStatus+" is not handled - iun="+res.sendMessageRequestDto.getIun()+" taxId="+LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID()), PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_IOINVALIDSTATUS));
                                }
                            }));
                });
    }
    
    private Mono<SendMessageResponseDto> manageOptIn(SendMessageRequestDto sendMessageRequestDto) {
        String hashedTaxId = DigestUtils.sha256Hex(sendMessageRequestDto.getRecipientTaxID());
        log.info("Managing send optin message taxId={} hashedTaxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), hashedTaxId, sendMessageRequestDto.getIun());
        return this.ioMessagesDao.get(hashedTaxId)
                .map(IOMessagesEntity::getLastModified)
                .defaultIfEmpty(Instant.EPOCH)
                .flatMap(lastSendDate -> {
                    if (lastSendDate.isBefore(Instant.now().minus(cfg.getIoOptinMinDays(), ChronoUnit.DAYS)))
                        return sendIOOptInMessage(sendMessageRequestDto)
                                .zipWhen(r -> ioMessagesDao.save(new IOMessagesEntity(hashedTaxId)).thenReturn(new Object()),
                                        (resp, nd) -> resp);
                    else
                    {
                        log.info("Not sending because a recent optin has already been sent taxId={} iun={}",  LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), sendMessageRequestDto.getIun());
                        SendMessageResponseDto res = new SendMessageResponseDto();
                        res.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_ALREADY_SENT);
                        return Mono.just(res);
                    }
                });
    }

    private Mono<SendMessageResponseDto> sendIOCourtesyMessage( SendMessageRequestDto sendMessageRequestDto, boolean localeIsIT ) {
        log.info( "Submit message taxId={} iun={} internalId={} recIndex={} carbonCopy={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), sendMessageRequestDto.getIun(), sendMessageRequestDto.getRecipientInternalID(), sendMessageRequestDto.getRecipientIndex(), sendMessageRequestDto.getCarbonCopyToDeliveryPush());
        if (cfg.isEnableIoMessage()) {
            FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
            MessageContent content = new MessageContent();

            // ora il subject è deciso da ext-registry
            String ioSubject = enrichWithSenderDenomination(cfg.getAppIoTemplate().getSubjectCourtesyAppIoMessage(), sendMessageRequestDto.getSenderDenomination());

            String truncatedIoSubject = ioSubject;
            if(ioSubject.length() > 120){
                truncatedIoSubject = ioSubject.substring(0, 120);
            }

            log.info("Get profile by post iun={}", sendMessageRequestDto.getIun());
            fiscalCodePayload.setFiscalCode(sendMessageRequestDto.getRecipientTaxID());
            if (sendMessageRequestDto.getDueDate()!=null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                content.setDueDate( fmt.format(sendMessageRequestDto.getDueDate() ));
            }
            content.setSubject( truncatedIoSubject );
            content.setMarkdown( localeIsIT?cfg.getAppIoTemplate().getMarkdownUpgradeAppIoITMessage():cfg.getAppIoTemplate().getMarkdownUpgradeAppIoENMessage());

            String requestAcceptedDate = null;

            if(sendMessageRequestDto.getRequestAcceptedDate() != null){
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                requestAcceptedDate = fmt.format( sendMessageRequestDto.getRequestAcceptedDate() );
            }
            content.setThirdPartyData( new ThirdPartyData()
                   .id( sendMessageRequestDto.getIun() )
                   .originalSender( sendMessageRequestDto.getSenderDenomination() )
                   .originalReceiptDate(requestAcceptedDate)
                   .summary( sendMessageRequestDto.getSubject() )
                   .configurationId( cfg.getIoRemoteContentCfgId() )
            );

            assert content.getThirdPartyData() != null;
            log.info( "Proceeding with send message iun={}", content.getThirdPartyData().getId());
            NewMessage m = new NewMessage();
            m.setFeatureLevelType("ADVANCED");
            m.setFiscalCode( fiscalCodePayload.getFiscalCode() );
            m.setContent( content );
            return courtesyMessageClient.submitMessageforUserWithFiscalCodeInBody(m)
                .map( response -> {
                    log.info( "Sent message iun={}", content.getThirdPartyData().getId());
                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setResult(SendMessageResponseDto.ResultEnum.SENT_COURTESY);
                    res.setId(response.getId());
                    return res;
                })
                .flatMap(sendMessageResponseDto -> sendIOMessageSentEventToDeliveyPush(sendMessageRequestDto, sendMessageResponseDto))
                .onErrorResume( exception ->{
                    log.error( "[DOWNSTREAM APPIO] Error in submitMessageforUserWithFiscalCodeInBody iun={}", content.getThirdPartyData().getId());
                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setResult(SendMessageResponseDto.ResultEnum.ERROR_COURTESY);
                    return Mono.just(res);
            });
        } else {
            log.warn( "Send IO message is disabled!!!" );
            SendMessageResponseDto res = new SendMessageResponseDto();
            res.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_COURTESY_DISABLED_BY_CONF);
            return Mono.just(res);
        }
    }

    @NotNull
    private Mono<SendMessageResponseDto> sendIOMessageSentEventToDeliveyPush(SendMessageRequestDto sendMessageRequestDto, SendMessageResponseDto sendMessageResponseDto) {
        if (Boolean.TRUE.equals(sendMessageRequestDto.getCarbonCopyToDeliveryPush())) {
            return sendIOSentMessageService.sendIOSentMessageNotification(sendMessageRequestDto.getIun(),
                            sendMessageRequestDto.getRecipientIndex(), sendMessageRequestDto.getRecipientInternalID(), Instant.now())
                    .thenReturn(sendMessageResponseDto);
        }
        else {
            return Mono.just(sendMessageResponseDto)
                    .doOnNext(res -> log.info("Send CC to delivery push not required"));
        }
    }

    private Mono<SendMessageResponseDto> sendIOOptInMessage( SendMessageRequestDto sendMessageRequestDto ) {
        log.info( "Submit activation message taxId={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()));
        if (cfg.isEnableIoActivationMessage()) {
            log.info( "Proceeding with send activation message taxId={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()));

            MessageContent content = new MessageContent();
            setContentFromDeliveryMode(sendMessageRequestDto, content);
            content.setSubject(cfg.getAppIoTemplate().getSubjectActivationAppIoMessage());

            NewMessage m = new NewMessage();
            m.setFeatureLevelType("ADVANCED");
            m.setFiscalCode( sendMessageRequestDto.getRecipientTaxID() );
            m.setContent( content );
            return optInClient.submitMessageforUserWithFiscalCodeInBody(m)
                .map( r -> {
                    log.info( "Sent activation message");

                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setResult(SendMessageResponseDto.ResultEnum.SENT_OPTIN);
                    res.setId(r.getId());
                    return res;
                }).onErrorResume( exception ->{
                        log.error( "Error in submitActivationMessageforUserWithFiscalCodeInBody iun={}", sendMessageRequestDto.getIun());
                        SendMessageResponseDto res = new SendMessageResponseDto();
                        res.setResult(SendMessageResponseDto.ResultEnum.ERROR_OPTIN);
                        return Mono.just(res);
                    });
        } else {
            log.info( "Send IO message is disabled" );
            SendMessageResponseDto res = new SendMessageResponseDto();
            res.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_DISABLED_BY_CONF);
            return Mono.just(res);
        }
    }

    private void setContentFromDeliveryMode(SendMessageRequestDto sendMessageRequestDto, MessageContent content) {
        //Per garantire la retrocompatibilità, se il valore di deliveryMode è null, il template da generare dovrà essere quello ANALOGICO.
        if (Objects.isNull(sendMessageRequestDto.getDeliveryMode()) || sendMessageRequestDto.getDeliveryMode().equals(SendMessageRequestDto.DeliveryModeEnum.ANALOG)) {
            log.info("Setting content for IO optIn message using ANALOG template");
            content.setMarkdown(composeFinalMarkdown(cfg.getAppIoTemplate().getMarkdownActivationAppIoAnalogMessage()));
        } else {
            log.info("Setting content for IO optIn message using DIGITAL template");
            content.setMarkdown(composeFinalMarkdown(cfg.getAppIoTemplate().getMarkdownActivationAppIoDigitalMessage()));
        }
    }

    private Mono<UserStatusResponseInternal> getUserStatusInternal(Mono<UserStatusRequestDto> body) {
        return body
                .flatMap( userStatusRequestDto -> {
                    String taxId = userStatusRequestDto.getTaxId();
                    log.info("Start getProfileByPOST taxId={}", LogUtils.maskTaxId(taxId));
                    FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                    fiscalCodePayload.setFiscalCode( taxId );

                    return courtesyMessageClient.getProfileByPOST( fiscalCodePayload ).map( res ->{
                        log.info("Response getProfileByPOST, user with taxId={} have AppIo activated and isUserAllowed={} preferredLangs={}", LogUtils.maskTaxId(taxId), res.getSenderAllowed(), res.getPreferredLanguages());

                        return UserStatusResponseInternal.builder()
                                .taxId(taxId)
                                .preferredLanguages(res.getPreferredLanguages())
                                .status( res.getSenderAllowed() ? UserStatusResponseInternal.StatusEnum.PN_ACTIVE : UserStatusResponseInternal.StatusEnum.PN_NOT_ACTIVE)
                                .build();

                    }).onErrorResume( WebClientResponseException.class, exception ->{
                        if(HttpStatus.NOT_FOUND.equals(exception.getStatusCode())){
                            log.info("Response status is 'NOT_FOUND' user with taxId={} haven't AppIo activated ", LogUtils.maskTaxId(taxId));
                            return Mono.just(UserStatusResponseInternal.builder()
                                    .taxId(taxId)
                                    .status(UserStatusResponseInternal.StatusEnum.APPIO_NOT_ACTIVE)
                                    .build()
                            );
                        }
                        log.error("Error in call getProfileByPOST ex={} for taxId={} ", exception, LogUtils.maskTaxId(taxId));
                        return Mono.just(UserStatusResponseInternal.builder()
                                .taxId(taxId)
                                .status(UserStatusResponseInternal.StatusEnum.ERROR)
                                .build()
                        );
                    });
                });
    }

    public Mono<UserStatusResponseDto> getUserStatus(Mono<UserStatusRequestDto> body) {
        return this.getUserStatusInternal(body)
                        .map(userStatusResponseInternal -> new UserStatusResponseDto()
                                .taxId(userStatusResponseInternal.getTaxId())
                                .status( UserStatusResponseDto.StatusEnum.fromValue(userStatusResponseInternal.getStatus().getValue())));
    }

    public Mono<PreconditionContentDto> notificationDisclaimer(String recipientInternalId, String iun) {
        String logMsg = String.format("notification disclaimer recipientInternalId=%s iun=%s", recipientInternalId, iun);
        PnAuditLogEvent logEvent = new PnAuditLogBuilder()
                .before(PnAuditLogEventType.AUD_NT_IO_DISCLAIM, logMsg )
                .build();
        logEvent.log();

        return notificationService.getSentNotificationPrivate(iun)
                .flatMap(deliveryResponse -> {
                    Integer recIndex = retrieveRecipientIndexFromInternalId(recipientInternalId, deliveryResponse);
                    return timelineService.getDeliveryInformation(iun, recIndex)
                            .doOnNext(timelineServiceResponse -> {
                                ExtendedDeliveryMode deliveryMode = timelineServiceResponse.getDeliveryMode();
                                if (deliveryMode == ExtendedDeliveryMode.UNKNOWN) {
                                    throw new PnNotFoundException("Delivery mode is UNKNOWN", "DeliveryModeInt.UNKNOWN for iun " + iun,
                                            PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_DELIVERY_MODE_UNKNOWN
                                    );
                                }
                            })
                            .map(timelineServiceResponse -> mapToPreconditionContent(
                                    timelineServiceResponse.getSchedulingAnalogDate(),
                                    iun,
                                    timelineServiceResponse.getRefinementOrViewedDate(),
                                    timelineServiceResponse.getDeliveryMode(),
                                    deliveryResponse.getSenderDenomination(),
                                    deliveryResponse.getSubject(),
                                    timelineServiceResponse.getIsNotificationCancelled()));
                })
                .doOnNext(preconditionContentDto -> log.info("[{}] [{}] PreconditionContentDto response: {}", iun, recipientInternalId, preconditionContentDto))
                .doOnNext(preconditionContentDto -> logEvent.generateSuccess().log())
                .doOnError( throwable -> logEvent.generateFailure("FAILURE {}", throwable.getMessage()).log() );
    }

    private static Integer retrieveRecipientIndexFromInternalId(String recipientInternalId, NotificationInt sentNotification) {
        List<NotificationRecipientInt> recipients = sentNotification.getRecipients();
        return range(0, recipients.size())
                .filter(i -> recipientInternalId.equals(recipients.get(i).getInternalId()))
                .findFirst()
                .orElseThrow(() -> new PnNotFoundException(
                        "Recipient not found",
                        "RecipientInternalId not found in notification recipients with iun " + sentNotification.getIun(),
                        ERROR_CODE_EXTERNALREGISTRIES_NOTIFICATION_RECIPIENT_ID_NOT_FOUND
                ));
    }

    private PreconditionContentDto mapToPreconditionContent(Instant schedulingAnalogDate,
                                                            String iun,
                                                            Instant refinementOrViewDate,
                                                            ExtendedDeliveryMode deliveryMode,
                                                            String senderDenomination,
                                                            String subject,
                                                            boolean isCancelled) {
        log.debug("Mapping to PreconditionContentDto with schedulingAnalogDate={}, iun={}, refinementOrViewDate={}, deliveryMode={}, senderDenomination={}, subject={}, isCancelled={}",
                schedulingAnalogDate, iun, refinementOrViewDate, deliveryMode, senderDenomination, subject,isCancelled);
        var preconditionContentInt = new PreconditionContentInt();
        BottomSheetContext bottomSheetContext = BottomSheetContext.builder()
                .schedulingAnalogDate(schedulingAnalogDate)
                .isCancelled(isCancelled)
                .senderDenomination(senderDenomination)
                .subject(subject)
                .iun(iun)
                .refinementOrViewDate(refinementOrViewDate)
                .deliveryMode(deliveryMode)
                .build();

        PreconditionContentInt processedInt = bottomSheetProcessorFactory.getBottomSheetProcessor(bottomSheetContext)
                .process(preconditionContentInt, bottomSheetContext, cfg);

        return mapFromPreconditionContentIntToDto(processedInt);
    }

    private PreconditionContentDto mapFromPreconditionContentIntToDto(PreconditionContentInt internal) {
        PreconditionContentDto dto = new PreconditionContentDto();
        dto.setTitle(internal.getTitle());
        dto.setMarkdown(internal.getMarkdown());
        return dto;
    }

    private String enrichWithSenderDenomination(String message, String senderDenomination) {
        return message.replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination);
    }

    private String composeFinalMarkdown(String markdown)
    {
        // per ora si fa una semplice sostituzione cablata sui nomi delle variabili
        return markdown
                .replace("${piattaformaNotificheURLCittadini}", cfg.getPiattaformanotificheurlCittadini())
                .replace("${piattaformaNotificheURLTOS}", cfg.getPiattaformanotificheurlTos())
                .replace("${piattaformaNotificheURLPrivacy}", cfg.getPiattaformanotificheurlPrivacy());
    }

    private boolean isPreferredLanguageIT(List<String> preferredLanguages)
    {
        return CollectionUtils.isEmpty(preferredLanguages) || preferredLanguages.contains(IO_LOCALE_IT_IT);
    }

}
