package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class PtDelegationEntityToPtInstitutionDtoMapper {

    private PtDelegationEntityToPtInstitutionDtoMapper() {}

    public static PtInstitutionDto toDto(List<PtDelegationEntity> events) {
        PtDelegationEntity latest = events.getFirst();
        PtInstitutionDto dto = new PtInstitutionDto();
        dto.setInstitutionId(latest.getInstitutionId());
        dto.setInstitutionName(latest.getInstitutionName());
        dto.setInstitutionTaxCode(latest.getInstitutionTaxCode());
        dto.setDelegationId(latest.getDelegationId());
        dto.setStatus(PtDelegationEntity.STATUS_DELETED.equals(latest.getStatus()) ? PtDelegationStatusDto.REVOKED : PtDelegationStatusDto.ACTIVE);
        dto.setStatusChangedAt(Date.from(statusChangedAt(events)));
        return dto;
    }

    private static Instant statusChangedAt(List<PtDelegationEntity> events) {
        String status = events.getFirst().getStatus();
        Instant statusChangedAt = events.getFirst().getLastChangeAt();
        for (PtDelegationEntity event : events) {
            if (!status.equals(event.getStatus())) {
                break;
            }
            statusChangedAt = event.getLastChangeAt();
        }
        return statusChangedAt;
    }
}
