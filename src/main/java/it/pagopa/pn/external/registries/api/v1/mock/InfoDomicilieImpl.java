package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class InfoDomicilieImpl {

    public static Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        if (RecipientTypeDto.PF.equals(recipientType)) {
            AnalogDomicileDto dto = new AnalogDomicileDto();
            dto.setAddress("ImmediateResponse(OK)");
            dto.setCap("40100");
            dto.setMunicipality("Bologna");
            dto.setProvince("BO");
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            String msg = String.format("recipientType '%s' not found", recipientType);
            throw new PnInternalException(msg);
        }
    }

    public static Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        if (RecipientTypeDto.PF.equals(recipientType)) {
            DigitalDomicileDto dto = new DigitalDomicileDto();
            dto.setAddress("nome.cognome.1@works.pec.it");
            dto.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.PEC);
            return Mono.just(ResponseEntity.ok().body(dto));
        } else {
            String msg = String.format("recipientType '%s' not found", recipientType);
            throw new PnInternalException(msg);
        }

    }
}
