package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class InfoDomicilieImpl {

    public static Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        AnalogDomicileDto dto = null;

        try {
            dto = MockResponsees.getMockResp().getOneAnalogDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            throw new PnInternalException("invalid mock file: " + MockResponsees.mockFile);
        }

        if (dto != null) {
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            String msg = String.format("recipientType '%s' - opaqueId '%s' not found", recipientType, opaqueId);
            throw new PnInternalException(msg);
        }
    }

    public static Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        DigitalDomicileDto dto = null;

        try {
            dto = MockResponsees.getMockResp().getOneDigitalDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            throw new PnInternalException("invalid mock file: " + MockResponsees.mockFile);
        }

        if (dto != null) {
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            String msg = String.format("recipientType '%s' - opaqueId '%s' not found", recipientType, opaqueId);
            throw new PnInternalException(msg);
        }
    }

}
