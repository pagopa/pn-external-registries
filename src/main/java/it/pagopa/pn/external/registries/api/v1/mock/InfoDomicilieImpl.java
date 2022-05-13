package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.exceptions.NotFoundException;
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
        AnalogDomicileDto dto = MockResponsees.getMockResp().getOneAnalogDomicile(recipientType.getValue(), opaqueId.toString());

        if (dto != null) {
            return Mono.just(dto);
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new NotFoundException();
        }
    }

    public  Mono<DigitalDomicileDto> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId) {
        DigitalDomicileDto dto = MockResponsees.getMockResp().getOneDigitalDomicile(recipientType.getValue(), opaqueId.toString());

        if (dto != null) {
            return Mono.just(dto);
        } else {
            log.warn("recipientType {} - opaqueId {} not found", recipientType, opaqueId);
            throw new NotFoundException();
        }
    }

}
