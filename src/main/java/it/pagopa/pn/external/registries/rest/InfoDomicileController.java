package it.pagopa.pn.external.registries.rest;

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
    @Override
    public Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        return InfoDomicileApi.super.getOneAnalogDomicile(recipientType, opaqueId, exchange);
    }

    @Override
    public Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        return InfoDomicileApi.super.getOneDigitalDomicile(recipientType, opaqueId, exchange);
    }
}
