package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationStatusDto;


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
