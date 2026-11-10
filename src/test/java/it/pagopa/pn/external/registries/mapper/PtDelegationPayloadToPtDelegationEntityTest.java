package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PtDelegationPayloadToPtDelegationEntityTest {

    @Test
    void toEntity() {
        Instant createdAt = Instant.parse("2026-11-02T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-11-20T09:30:00Z");
        Instant closedAt = Instant.parse("2026-11-20T09:30:00Z");

        PnPtDelegationPayload payload = new PnPtDelegationPayload();
        payload.setDelegationId("delegation-1");
        payload.setDelegationType("PT");
        payload.setProductId("prod-pn");
        payload.setStatus(PtDelegationEntity.STATUS_DELETED);
        payload.setIsTest(true);
        payload.setSourceEventId("source-event-1");
        payload.setSourceEventType("UPDATE");
        payload.setCreatedAt(createdAt);
        payload.setUpdatedAt(updatedAt);
        payload.setClosedAt(closedAt);
        payload.setInstitutionId("ente-A");
        payload.setInstitutionName("Comune di Test");
        payload.setInstitutionRootName("Ente Centrale");
        payload.setInstitutionTaxCode("01199250158");
        payload.setInstitutionType("PA");
        payload.setPtId("pt-1");
        payload.setPtName("Partner Tecnologico");
        payload.setPtTaxCode("12345678901");
        payload.setPtType("PT");

        PtDelegationEntity entity = PtDelegationPayloadToPtDelegationEntity.toEntity(payload, updatedAt);

        assertThat(entity.getPtId()).isEqualTo("pt-1");
        assertThat(entity.getEventKey()).isEqualTo("ente-A#2026-11-20T09:30:00.000000Z");
        assertThat(entity.getDelegationId()).isEqualTo("delegation-1");
        assertThat(entity.getDelegationType()).isEqualTo("PT");
        assertThat(entity.getProductId()).isEqualTo("prod-pn");
        assertThat(entity.getStatus()).isEqualTo(PtDelegationEntity.STATUS_DELETED);
        assertThat(entity.getIsTest()).isTrue();
        assertThat(entity.getSourceEventId()).isEqualTo("source-event-1");
        assertThat(entity.getSourceEventType()).isEqualTo("UPDATE");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getClosedAt()).isEqualTo(closedAt);
        assertThat(entity.getLastChangeAt()).isEqualTo(updatedAt);
        assertThat(entity.getInstitutionId()).isEqualTo("ente-A");
        assertThat(entity.getInstitutionName()).isEqualTo("Comune di Test");
        assertThat(entity.getInstitutionRootName()).isEqualTo("Ente Centrale");
        assertThat(entity.getInstitutionTaxCode()).isEqualTo("01199250158");
        assertThat(entity.getInstitutionType()).isEqualTo("PA");
        assertThat(entity.getPtName()).isEqualTo("Partner Tecnologico");
        assertThat(entity.getPtTaxCode()).isEqualTo("12345678901");
        assertThat(entity.getPtType()).isEqualTo("PT");
    }
}
