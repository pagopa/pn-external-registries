package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.api.v1.mock.InfoDomicilieImpl;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.api.InfoDomicileApi;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
public class InfoDomicileController implements InfoDomicileApi {

    private final InfoDomicilieImpl infoDomicilie;

    public InfoDomicileController(InfoDomicilieImpl infoDomicilie) {
        this.infoDomicilie = infoDomicilie;
    }

    /**
     * GET /ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/analog : Read the analog domicile of a notification recipient
     * Read the analog domicile of a notification recipient. Il destinatario  può essere una persona fisica o una persona giuridica.
     *
     * @param recipientType il tipo del destinatario (required)
     * @param opaqueId Identificativo universale univoco del destinatario (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        log.debug("getOneAnalogDomicile - recipientType = {} - opaqueId = {}", recipientType, opaqueId);

        return infoDomicilie.getOneAnalogDomicile(recipientType, opaqueId)
                .map(m -> ResponseEntity.ok().body(m));
    }

    /**
     * GET /ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/digital : Read the digital domicile of a notification recipient
     * Read the digital domicile of a notification recipient. Il destinatario  può essere una persona fisica o una persona giuridica.
     *
     * @param recipientType il tipo del destinatario (required)
     * @param opaqueId Identificativo universale univoco del destinatario (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        log.debug("getOneDigitalDomicile - recipientType = {} - opaqueId = {}", recipientType, opaqueId);

        return infoDomicilie.getOneDigitalDomicile(recipientType, opaqueId)
                .map(m -> ResponseEntity.ok().body(m));
    }
}
