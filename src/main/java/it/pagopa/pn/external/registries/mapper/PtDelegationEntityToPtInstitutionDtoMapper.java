package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;

public class PtDelegationEntityToPtInstitutionDtoMapper {

    private PtDelegationEntityToPtInstitutionDtoMapper() {}

    public static PtInstitutionDto toDto(PtDelegationEntity entity) {
        throw new UnsupportedOperationException();
    }
}
