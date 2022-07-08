package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.institutions.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;

public class InstitutionResourceDtoToPaSummaryDto {

    private InstitutionResourceDtoToPaSummaryDto() {}

    public static PaSummaryDto toDto(InstitutionResourceDto entity) {
        PaSummaryDto dto = new PaSummaryDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        return  dto;
    }
}
