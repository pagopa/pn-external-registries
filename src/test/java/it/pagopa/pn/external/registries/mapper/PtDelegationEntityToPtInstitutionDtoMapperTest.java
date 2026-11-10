package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PtDelegationEntityToPtInstitutionDtoMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-02T10:00:00.000000Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-11-20T09:30:00.000000Z");
    private static final Instant REACTIVATED_AT = Instant.parse("2026-12-01T15:12:00.000000Z");
    private static final Instant RENAMED_AT = Instant.parse("2026-12-10T08:00:00.000000Z");

    @Test
    void toDtoActive() {
        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals("ente-A", dto.getInstitutionId());
        assertEquals("Comune di A", dto.getInstitutionName());
        assertEquals("00000000000", dto.getInstitutionTaxCode());
        assertEquals("delegation-A", dto.getDelegationId());
        assertEquals(PtDelegationStatusDto.ACTIVE, dto.getStatus());
        assertEquals(Date.from(CREATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoActiveUpdatedWithoutStatusChange() {
        PtDelegationEntity renamed = newEntity(PtDelegationEntity.STATUS_ACTIVE, UPDATED_AT);
        renamed.setInstitutionName("Comune di A rinominato");

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                renamed,
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals("Comune di A rinominato", dto.getInstitutionName());
        assertEquals(PtDelegationStatusDto.ACTIVE, dto.getStatus());
        assertEquals(Date.from(CREATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoReactivated() {
        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                newEntity(PtDelegationEntity.STATUS_ACTIVE, REACTIVATED_AT),
                newEntity(PtDelegationEntity.STATUS_DELETED, UPDATED_AT),
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals(PtDelegationStatusDto.ACTIVE, dto.getStatus());
        assertEquals(Date.from(REACTIVATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoReactivatedAndUpdatedWithoutStatusChange() {
        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                newEntity(PtDelegationEntity.STATUS_ACTIVE, RENAMED_AT),
                newEntity(PtDelegationEntity.STATUS_ACTIVE, REACTIVATED_AT),
                newEntity(PtDelegationEntity.STATUS_DELETED, UPDATED_AT),
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals(PtDelegationStatusDto.ACTIVE, dto.getStatus());
        assertEquals(Date.from(REACTIVATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoRevoked() {
        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                newEntity(PtDelegationEntity.STATUS_DELETED, UPDATED_AT),
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals(PtDelegationStatusDto.REVOKED, dto.getStatus());
        assertEquals(Date.from(UPDATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoRevokedUpdatedWithoutStatusChange() {
        PtDelegationEntity renamed = newEntity(PtDelegationEntity.STATUS_DELETED, RENAMED_AT);
        renamed.setClosedAt(UPDATED_AT);

        PtInstitutionDto dto = PtDelegationEntityToPtInstitutionDtoMapper.toDto(List.of(
                renamed,
                newEntity(PtDelegationEntity.STATUS_DELETED, UPDATED_AT),
                newEntity(PtDelegationEntity.STATUS_ACTIVE, CREATED_AT)));

        assertEquals(PtDelegationStatusDto.REVOKED, dto.getStatus());
        assertEquals(Date.from(UPDATED_AT), dto.getStatusChangedAt());
    }

    @Test
    void toDtoUnsupportedStatus() {
        List<PtDelegationEntity> events = List.of(newEntity("SUSPENDED", UPDATED_AT));

        assertThrows(IllegalArgumentException.class, () -> PtDelegationEntityToPtInstitutionDtoMapper.toDto(events));
    }

    @Test
    void isSupportedStatus() {
        assertTrue(PtDelegationEntityToPtInstitutionDtoMapper.isSupportedStatus(PtDelegationEntity.STATUS_ACTIVE));
        assertTrue(PtDelegationEntityToPtInstitutionDtoMapper.isSupportedStatus(PtDelegationEntity.STATUS_DELETED));
        assertFalse(PtDelegationEntityToPtInstitutionDtoMapper.isSupportedStatus("SUSPENDED"));
        assertFalse(PtDelegationEntityToPtInstitutionDtoMapper.isSupportedStatus(null));
    }

    private PtDelegationEntity newEntity(String status, Instant lastChangeAt) {
        PtDelegationEntity entity = new PtDelegationEntity();
        entity.setPtId("pt-1");
        entity.setEventKey(PtDelegationEntity.buildEventKey("ente-A", lastChangeAt));
        entity.setInstitutionId("ente-A");
        entity.setInstitutionName("Comune di A");
        entity.setInstitutionTaxCode("00000000000");
        entity.setDelegationId("delegation-A");
        entity.setStatus(status);
        entity.setCreatedAt(CREATED_AT);
        entity.setLastChangeAt(lastChangeAt);
        if (!CREATED_AT.equals(lastChangeAt)) {
            entity.setUpdatedAt(lastChangeAt);
        }
        if (PtDelegationEntity.STATUS_DELETED.equals(status)) {
            entity.setClosedAt(lastChangeAt);
        }
        return entity;
    }
}
