package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.MessageContent;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.ThirdPartyData;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.middleware.db.io.dao.OptInSentDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.OptInSentEntity;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOClient;
import it.pagopa.pn.external.registries.services.io.dto.UserStatusResponseInternal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class IOService {

    private static final String IO_LOCALE_IT_IT = "it_IT";
    private final IOClient client;

    private final PnExternalRegistriesConfig cfg;

    private final OptInSentDao optInSentDao;

    public IOService(IOClient client, PnExternalRegistriesConfig cfg, OptInSentDao optInSentDao) {
        this.client = client;
        this.cfg = cfg;
        this.optInSentDao = optInSentDao;
    }

    public Mono<SendMessageResponseDto> sendIOMessage( Mono<SendMessageRequestDto> sendMessageRequestDtoMono )
    {
        return sendMessageRequestDtoMono
                .map(sendMessageRequestDto -> {
                    log.info("[enter] sendIoMessage taxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), sendMessageRequestDto.getIun());
                    return sendMessageRequestDto;
                })
                .zipWhen(sendMessageRequestDto -> {
                            UserStatusRequestDto requestDto = new UserStatusRequestDto().taxId(sendMessageRequestDto.getRecipientTaxID());
                            return this.getUserStatusInternal( Mono.just(requestDto) );
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
                            return Mono.error(new PnInternalException("ioStatus="+ioStatus+" is not handled - iun="+res.sendMessageRequestDto.getIun()+" taxId="+LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID())));
                    }
                });
    }
    
    private Mono<SendMessageResponseDto> manageOptIn(SendMessageRequestDto sendMessageRequestDto) {
        String hashedTaxId = DigestUtils.sha256Hex(sendMessageRequestDto.getRecipientTaxID());
        log.info("Managing send optin message taxId={} hashedTaxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), hashedTaxId, sendMessageRequestDto.getIun());
        return this.optInSentDao.get(hashedTaxId)
                .map(OptInSentEntity::getLastModified)
                .defaultIfEmpty(Instant.EPOCH)
                .flatMap(lastSendDate -> {
                    if (lastSendDate.isBefore(Instant.now().minus(cfg.getIoOptinMinDays(), ChronoUnit.DAYS)))
                        return sendIOOptInMessage(sendMessageRequestDto)
                                .zipWhen(r -> optInSentDao.save(new OptInSentEntity(hashedTaxId)),
                                        (resp, nd) -> resp);
                    else
                    {
                        log.info("Not sending because a recent optin has already been sent taxId={} iun={}",  LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), sendMessageRequestDto.getIun());
                        return Mono.empty();
                    }
                });
    }

    private Mono<SendMessageResponseDto> sendIOCourtesyMessage( SendMessageRequestDto sendMessageRequestDto, boolean localeIsIT ) {
        log.info( "Submit message taxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), sendMessageRequestDto.getIun());
        if (cfg.isEnableIoMessage()) {
            FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
            MessageContent content = new MessageContent();

            String ioSubject = sendMessageRequestDto.getSubject() + "-" + sendMessageRequestDto.getSenderDenomination();

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
            );

            log.info( "Proceeding with send message iun={}", content.getThirdPartyData().getId());
            NewMessage m = new NewMessage();
            m.setFeatureLevelType("ADVANCED");
            m.setFiscalCode( fiscalCodePayload.getFiscalCode() );
            m.setContent( content );
            return client.submitMessageforUserWithFiscalCodeInBody(m)
                .map( response -> {
                    log.info( "Sent message iun={}", content.getThirdPartyData().getId());
                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setResult(SendMessageResponseDto.ResultEnum.SENT_COURTESY);
                    res.setId(response.getId());
                    return res;
                }).onErrorResume( exception ->{
                        log.error( "Error in submitMessageforUserWithFiscalCodeInBody iun={}", content.getThirdPartyData().getId());
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

    private Mono<SendMessageResponseDto> sendIOOptInMessage( SendMessageRequestDto sendMessageRequestDto ) {
        log.info( "Submit activation message taxId={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()));
        if (cfg.isEnableIoActivationMessage()) {
            log.info( "Proceeding with send activation message taxId={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()));

            MessageContent content = new MessageContent();
            content.setMarkdown( cfg.getAppIoTemplate().getMarkdownActivationAppIoMessage() );
            content.setSubject(cfg.getAppIoTemplate().getSubjectActivationAppIoMessage());

            NewMessage m = new NewMessage();
            m.setFeatureLevelType("ADVANCED");
            m.setFiscalCode( sendMessageRequestDto.getRecipientTaxID() );
            m.setContent( content );
            return client.submitActivationMessageforUserWithFiscalCodeInBody(m)
                .map( r -> {
                    log.info( "Sent activation message");

                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setResult(SendMessageResponseDto.ResultEnum.SENT_OPTIN);
                    res.setId(r.getId());
                    return res;
                }).onErrorResume( exception ->{
                        log.error( "Error in submitActivationMessageforUserWithFiscalCodeInBody iun={}", content.getThirdPartyData().getId());
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

    private Mono<UserStatusResponseInternal> getUserStatusInternal(Mono<UserStatusRequestDto> body) {
        return body
                .flatMap( userStatusRequestDto -> {
                    String taxId = userStatusRequestDto.getTaxId();
                    log.info("Start getProfileByPOST taxId={}", LogUtils.maskTaxId(taxId));
                    FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                    fiscalCodePayload.setFiscalCode( taxId );

                    return client.getProfileByPOST( fiscalCodePayload ).map( res ->{
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

    private boolean isPreferredLanguageIT(List<String> preferredLanguages)
    {
        return CollectionUtils.isEmpty(preferredLanguages) || preferredLanguages.contains(IO_LOCALE_IT_IT);
    }
}
