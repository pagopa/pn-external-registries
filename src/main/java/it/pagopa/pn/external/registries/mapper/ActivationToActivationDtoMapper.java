package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationStatusDto;
import org.springframework.stereotype.Component;

@Component
public class ActivationToActivationDtoMapper {

    private ActivationToActivationDtoMapper(){}

    public static ActivationDto toDto(Activation r) {
        ActivationDto activationDto = new ActivationDto();
        activationDto.setFiscalCode(r.getFiscalCode());
        activationDto.setStatus(ActivationStatusDto.fromValue(r.getStatus()));
        activationDto.setVersion(r.getVersion());
        return  activationDto;
    }
}
