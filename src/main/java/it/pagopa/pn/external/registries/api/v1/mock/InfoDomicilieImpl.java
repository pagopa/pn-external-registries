package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class InfoDomicilieImpl {

    public  Mono<AnalogDomicileDto> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId) {
        AnalogDomicileDto dto;

        try {
            dto = MockResponsees.getMockResp().getOneAnalogDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            log.error("exception on mock", e);
            throw new InternalErrorException();
        }

        if (dto != null) {
            return Mono.just(dto);
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new InternalErrorException();
        }
    }

    public  Mono<DigitalDomicileDto> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId) {
        DigitalDomicileDto dto;

        try {
            dto = MockResponsees.getMockResp().getOneDigitalDomicile(recipientType.getValue(), opaqueId.toString());
        } catch (Exception e) {
            log.error("exception on mock", e);
            throw new InternalErrorException();
        }

        if (dto != null) {
            return Mono.just(dto);
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new InternalErrorException();
        }
    }

}
