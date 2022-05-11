package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
public class InfoDomicilieImpl {

    public static Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        AnalogDomicileDto dto = null;

        try {
            dto = MockResponsees.getMockResp().getOneAnalogDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            log.error("exception on mock", e);
            throw new InternalErrorException();
        }

        if (dto != null) {
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new InternalErrorException();
        }
    }

    public static Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        DigitalDomicileDto dto = null;

        try {
            dto = MockResponsees.getMockResp().getOneDigitalDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            log.error("exception on mock", e);
            throw new InternalErrorException();
        }

        if (dto != null) {
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new InternalErrorException();
        }
    }

}
