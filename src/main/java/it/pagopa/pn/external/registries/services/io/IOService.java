package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.MessageContent;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.ThirdPartyData;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendActivationMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class IOService {

    private final IOClient client;

    private final PnExternalRegistriesConfig cfg;


    public IOService(IOClient client, PnExternalRegistriesConfig cfg) {
        this.client = client;
        this.cfg = cfg;
    }

    public Mono<SendMessageResponseDto> sendIOMessage( Mono<SendMessageRequestDto> sendMessageRequestDto ) {
        FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
        MessageContent content = new MessageContent();
        if (cfg.isEnableIoMessage()) {
            return sendMessageRequestDto
                    .map(r -> {
                        log.info( "Submit message taxId={} iun={}", LogUtils.maskTaxId(r.getRecipientTaxID()), r.getIun());
                        return r;
                    })
                    .flatMap( r -> {

                        String ioSubject = r.getSubject() + "-" + r.getSenderDenomination();

                        String truncatedIoSubject = ioSubject;
                        if(ioSubject.length() > 120){
                            truncatedIoSubject = ioSubject.substring(0, 120);
                        }
                        
                        log.info("Get profile by post iun={}", r.getIun());
                        fiscalCodePayload.setFiscalCode(r.getRecipientTaxID());
                        if (r.getDueDate()!=null) {
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            content.setDueDate( fmt.format(r.getDueDate() ));
                        }
                        content.setSubject( truncatedIoSubject );
                        content.setMarkdown( cfg.getAppIoTemplate().getMarkdownUpgradeAppIoMessage() );

                        String requestAcceptedDate = null;
                        
                        if(r.getRequestAcceptedDate() != null){
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            requestAcceptedDate = fmt.format( r.getRequestAcceptedDate() );
                        }
                        content.setThirdPartyData( new ThirdPartyData()
                               .id( r.getIun() )
                               .originalSender( r.getSenderDenomination() )
                               .originalReceiptDate(requestAcceptedDate)
                               .summary( r.getSubject() )
                        );
                        return client.getProfileByPOST(fiscalCodePayload);
                    })
                    .flatMap( r -> {
                        log.info( "Proceeding with send message iun={}", content.getThirdPartyData().getId());
                        NewMessage m = new NewMessage();
                        m.setFeatureLevelType("ADVANCED");
                        m.setFiscalCode( fiscalCodePayload.getFiscalCode() );
                        m.setContent( content );
                        return client.submitMessageforUserWithFiscalCodeInBody(m);
                    })
                    .map( r -> {
                        log.info( "Sent message iun={}", content.getThirdPartyData().getId());
                        SendMessageResponseDto res = new SendMessageResponseDto();
                        res.setId(r.getId());
                        return res;
                    });
        } else {
            log.info( "Send IO message is disabled" );
            return Mono.empty();
        }
    }

    public Mono<SendMessageResponseDto> sendIOActivationMessage( Mono<SendActivationMessageRequestDto> sendMessageRequestDto ) {
        if (cfg.isEnableIoActivationMessage()) {
            return sendMessageRequestDto
                    .map(r -> {
                        log.info( "Submit activation message taxId={}", LogUtils.maskTaxId(r.getRecipientTaxID()));
                        return r;
                    })
                    .zipWhen( r -> {
                        FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                        fiscalCodePayload.setFiscalCode(r.getRecipientTaxID());
                        return client.getProfileByPOST(fiscalCodePayload);
                    }, (r, a) ->r)
                    .flatMap( r -> {
                        log.info( "Proceeding with send activation message taxId={}", LogUtils.maskTaxId(r.getRecipientTaxID()));

                        MessageContent content = new MessageContent();
                        content.setMarkdown( cfg.getAppIoTemplate().getMarkdownActivationAppIoMessage() );
                        content.setSubject(cfg.getAppIoTemplate().getSubjectActivationAppIoMessage());

                        NewMessage m = new NewMessage();
                        m.setFeatureLevelType("ADVANCED");
                        m.setFiscalCode( r.getRecipientTaxID() );
                        m.setContent( content );
                        return client.submitActivationMessageforUserWithFiscalCodeInBody(m);
                    })
                    .map( r -> {
                        log.info( "Sent activation message");

                        SendMessageResponseDto res = new SendMessageResponseDto();
                        res.setId(r.getId());
                        return res;
                    });
        } else {
            log.info( "Send IO message is disabled" );
            return Mono.empty();
        }
    }
}
