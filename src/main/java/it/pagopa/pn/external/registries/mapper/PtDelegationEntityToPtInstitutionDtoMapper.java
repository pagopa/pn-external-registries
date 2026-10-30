package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;

import java.time.Instant;
import java.util.Date;

public class PtDelegationEntityToPtInstitutionDtoMapper {

    private PtDelegationEntityToPtInstitutionDtoMapper() {}

    public static PtInstitutionDto toDto(PtDelegationEntity entity) {
        PtInstitutionDto dto = new PtInstitutionDto();
        dto.setInstitutionId(entity.getInstitutionId());
        dto.setInstitutionName(entity.getInstitutionName());
        dto.setInstitutionTaxCode(entity.getInstitutionTaxCode());
        dto.setDelegationId(entity.getDelegationId());
        if (PtDelegationEntity.STATUS_DELETED.equals(entity.getStatus())) {
            dto.setStatus(PtDelegationStatusDto.REVOKED);
            dto.setStatusChangedAt(toDate(entity.getClosedAt(), entity.getLastChangeAt()));
        } else {
            dto.setStatus(PtDelegationStatusDto.ACTIVE);
            dto.setStatusChangedAt(toDate(entity.getCreatedAt(), entity.getLastChangeAt()));
        }
        return dto;
    }

    private static Date toDate(Instant date, Instant fallback) {
        return Date.from(date != null ? date : fallback);
    }
}
