package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PtDelegationEntityToPtInstitutionDtoMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-02T10:00:00.000000Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-11-20T09:30:00.000000Z");

    @Test
    void toDtoActive() {
        PtDelegationEntity entity = newEntity(PtDelegationEntity.STATUS_ACTIVE);
        entity.setUpdatedAt(UPDATED_AT);
        entity.setLastChangeAt(UPDATED_AT);

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(entity);

        assertEquals("ente-A", dto.getInstitutionId());
        assertEquals("Comune di A", dto.getInstitutionName());
        assertEquals("00000000000", dto.getInstitutionTaxCode());
        assertEquals("delegation-A", dto.getDelegationId());
        assertEquals(PtDelegationStatusDto.ACTIVE, dto.getStatus());
        assertEquals(Date.from(CREATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoRevoked() {
        PtDelegationEntity entity = newEntity(PtDelegationEntity.STATUS_DELETED);
        entity.setUpdatedAt(UPDATED_AT);
        entity.setClosedAt(UPDATED_AT);
        entity.setLastChangeAt(UPDATED_AT);

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(entity);

        assertEquals(PtDelegationStatusDto.REVOKED, dto.getStatus());
        assertEquals(Date.from(UPDATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoRevokedWithoutClosedAt() {
        PtDelegationEntity entity = newEntity(PtDelegationEntity.STATUS_DELETED);
        entity.setLastChangeAt(UPDATED_AT);

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(entity);

        assertEquals(Date.from(UPDATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoActiveWithoutCreatedAt() {
        PtDelegationEntity entity = newEntity(PtDelegationEntity.STATUS_ACTIVE);
        entity.setCreatedAt(null);
        entity.setLastChangeAt(UPDATED_AT);

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(entity);

        assertEquals(Date.from(UPDATED_AT), dto.getStatusChangedAt());
    }

    private PtDelegationEntity newEntity(String status) {
        PtDelegationEntity entity = new PtDelegationEntity();
        entity.setPtId("pt-1");
        entity.setInstitutionId("ente-A");
        entity.setInstitutionName("Comune di A");
        entity.setInstitutionTaxCode("00000000000");
        entity.setDelegationId("delegation-A");
        entity.setStatus(status);
        entity.setCreatedAt(CREATED_AT);
        entity.setLastChangeAt(CREATED_AT);
        return entity;
    }
}
