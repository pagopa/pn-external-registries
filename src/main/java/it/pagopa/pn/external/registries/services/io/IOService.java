package it.pagopa.pn.external.registries.services.io;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class IOService {

    private final IOClient client;

    private final PnExternalRegistriesConfig cfg;

    private final OptInSentDao optInSentDao;

    public IOService(IOClient client, PnExternalRegistriesConfig cfg, OptInSentDao optInSentDao) {
        this.client = client;
        this.cfg = cfg;
        this.optInSentDao = optInSentDao;
    }

    public Mono<SendMessageResponseDto> sendIOMessage( Mono<SendMessageRequestDto> sendMessageRequestDto )
    {
        return sendMessageRequestDto
                .map(sendMessageRequestDto1 -> {
                    log.info("[enter] sendIoMessage taxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto1.getRecipientTaxID()), sendMessageRequestDto1.getIun());
                    return sendMessageRequestDto1;
                })
                .zipWhen(sendMessageRequestDto1 -> this.getIOActivationStatus(sendMessageRequestDto1.getRecipientTaxID()),
                    (sendMessageRequestDto0, iostatus0) -> new Object(){
                        public final String ioStatus = iostatus0;   // TODO enum status
                        public final SendMessageRequestDto sendMessageRequestDto = sendMessageRequestDto0;
                    })
                .flatMap(res -> {
                        if (res.ioStatus == "nondisponibile")   // TODO sistema col check enum
                        {
                            log.info("IO is not available for user, not sending message taxId={} iun={}", LogUtils.maskTaxId(res.sendMessageRequestDto.getRecipientTaxID()), res.sendMessageRequestDto.getIun());
                            SendMessageResponseDto sendres = new SendMessageResponseDto();
                            sendres.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_APPIO_UNAVAILABLE);
                            return Mono.just(sendres);
                        }
                        else if (res.ioStatus == "abilitato_pn") // TODO sistema col check enum
                            return sendIOCourtesyMessage(res.sendMessageRequestDto);
                        else
                            return manageOptIn(res.sendMessageRequestDto);
                });
    }

    public Mono<String> getIOActivationStatus(String taxId)
    {
        // TODO usa il tuo metodo che tornerà un enum immagino
        //this.getprofiles(taxId).................
        return Mono.just("");
    }

    private Mono<SendMessageResponseDto> manageOptIn(SendMessageRequestDto sendMessageRequestDto) {
        String hashedTaxId = DigestUtils.sha256Hex(sendMessageRequestDto.getRecipientTaxID());
        log.info("Managing send optin message taxId={} hashedTaxId={} iun={}", LogUtils.maskTaxId(sendMessageRequestDto.getRecipientTaxID()), hashedTaxId, sendMessageRequestDto.getIun());
        return this.optInSentDao.get(hashedTaxId)
                .map(ent -> ent.getLastModified())
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

    private Mono<SendMessageResponseDto> sendIOCourtesyMessage( SendMessageRequestDto sendMessageRequestDto ) {
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
            content.setMarkdown( cfg.getAppIoTemplate().getMarkdownUpgradeAppIoMessage() );

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
                });
        } else {
            log.info( "Send IO message is disabled" );
            SendMessageResponseDto res = new SendMessageResponseDto();
            res.setResult(SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_DISABLED_BY_CONF);
            return Mono.just(res);
        }
    }

    public Mono<UserStatusResponseDto> getUserStatus(Mono<UserStatusRequestDto> body) {
        return body
                .flatMap( userStatusRequestDto -> {
                    String taxId = userStatusRequestDto.getTaxId();
                    log.info("Start getProfileByPOST taxId={}", LogUtils.maskTaxId(taxId));
                    FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                    fiscalCodePayload.setFiscalCode( taxId );

                    return client.getProfileByPOST( fiscalCodePayload ).map( res ->{
                        log.info("Response getProfileByPOST, user with taxId={} have AppIo activated and isUserAllowed={}", LogUtils.maskTaxId(taxId), res.getSenderAllowed());

                        return new UserStatusResponseDto()
                                .taxId(taxId)
                                .status( res.getSenderAllowed() ? UserStatusResponseDto.StatusEnum.PN_ACTIVE : UserStatusResponseDto.StatusEnum.PN_NOT_ACTIVE);

                    }).onErrorResume( WebClientResponseException.class, exception ->{
                        if(HttpStatus.NOT_FOUND.equals(exception.getStatusCode())){
                            log.info("Response status is 'NOT_FOUND' user with taxId={} haven't AppIo activated ", LogUtils.maskTaxId(taxId));
                            return Mono.just( new UserStatusResponseDto()
                                    .taxId(taxId)
                                    .status(UserStatusResponseDto.StatusEnum.APPIO_NOT_ACTIVE)
                            );
                        }
                        log.error("Error in call getProfileByPOST ex={} for taxId={} ", exception, LogUtils.maskTaxId(taxId));
                        return Mono.error(exception);
                    });
                });
    }

}
