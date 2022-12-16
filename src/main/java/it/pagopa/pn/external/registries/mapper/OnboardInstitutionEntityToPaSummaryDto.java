package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;

public class OnboardInstitutionEntityToPaSummaryDto {

    private OnboardInstitutionEntityToPaSummaryDto() {}

    public static PaSummaryDto toDto(OnboardInstitutionEntity entity) {
        PaSummaryDto dto = new PaSummaryDto();
        dto.setId(entity.getInstitutionId());
        dto.setName(entity.getDescription());

        return  dto;
    }
}
