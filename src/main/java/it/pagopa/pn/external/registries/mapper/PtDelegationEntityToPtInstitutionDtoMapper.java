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
        dto.setStatus(toStatus(latest.getStatus()));
        dto.setStatusChangedAt(Date.from(statusChangedAt(events)));
        return dto;
    }

    public static boolean isSupportedStatus(String status) {
        return PtDelegationEntity.STATUS_ACTIVE.equals(status) || PtDelegationEntity.STATUS_DELETED.equals(status);
    }

    private static PtDelegationStatusDto toStatus(String status) {
        return switch (status) {
            case PtDelegationEntity.STATUS_ACTIVE -> PtDelegationStatusDto.ACTIVE;
            case PtDelegationEntity.STATUS_DELETED -> PtDelegationStatusDto.REVOKED;
            default -> throw new IllegalArgumentException("Unsupported delegation status " + status);
        };
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
