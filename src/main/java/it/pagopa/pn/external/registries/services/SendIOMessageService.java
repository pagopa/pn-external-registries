package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.IOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Service
@Slf4j
public class SendIOMessageService {

    private final IOClient client;

    private static final String MARKDOWN_MESSAGE = "Per visualizzare il contenuto del certificato,  **aggiorna IO all'ultima versione disponibile**:\\n\\n- [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\\n\\n- [Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)\\n\\n***\\n\\nIn order to visualize your EU Digital Covid Certificate, **you have to update IO to the last available version**:\\n\\n- [Update for Android devices](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\\n\\n- [Update for iOS devices](https://apps.apple.com/it/app/io/id1501681835)";

    public SendIOMessageService(IOClient client) {
        this.client = client;
    }

    public Mono<SendMessageResponseDto> sendIOMessage( Mono<SendMessageRequestDto> sendMessageRequestDto ) {
        FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
        MessageContent content = new MessageContent();
        return sendMessageRequestDto
                .flatMap( r -> {
                    log.info("Get profile by post iun={}", r.getIun());
                    fiscalCodePayload.setFiscalCode(r.getRecipientTaxID());
                    content.setDueDate(Timestamp.from( r.getDueDate().toInstant() ) );
                    content.setSubject( r.getSubject() );
                    content.setMarkdown( MARKDOWN_MESSAGE );
                    content.setThirdPartyData( new ThirdPartyData()
                            .id( r.getIun() )
                            .originalSender( r.getSenderDenomination() ));
                    return client.getProfileByPOST(fiscalCodePayload);
                })
                .flatMap( r -> {
                    log.info( "Submit message" );
                    NewMessage m = new NewMessage();
                    m.setFiscalCode( fiscalCodePayload.getFiscalCode() );
                    m.setContent( content );
                    return client.submitMessageforUserWithFiscalCodeInBody(m);
                })
                .map( r -> {
                    SendMessageResponseDto res = new SendMessageResponseDto();
                    res.setId(r.getId());
                    return res;
                });
    }
}
