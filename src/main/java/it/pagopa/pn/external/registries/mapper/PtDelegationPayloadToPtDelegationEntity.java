package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;

import java.time.Instant;

public class PtDelegationPayloadToPtDelegationEntity {

    private PtDelegationPayloadToPtDelegationEntity() {}

    public static PtDelegationEntity toEntity(PnPtDelegationPayload payload, Instant lastChangeAt) {
        PtDelegationEntity entity = new PtDelegationEntity();
        entity.setPtId(payload.getPtId());
        entity.setEventKey(PtDelegationEntity.buildEventKey(payload.getInstitutionId(), lastChangeAt));
        entity.setDelegationId(payload.getDelegationId());
        entity.setDelegationType(payload.getDelegationType());
        entity.setProductId(payload.getProductId());
        entity.setStatus(payload.getStatus());
        entity.setIsTest(payload.getIsTest());
        entity.setSourceEventId(payload.getSourceEventId());
        entity.setSourceEventType(payload.getSourceEventType());
        entity.setCreatedAt(payload.getCreatedAt());
        entity.setUpdatedAt(payload.getUpdatedAt());
        entity.setClosedAt(payload.getClosedAt());
        entity.setLastChangeAt(lastChangeAt);
        entity.setInstitutionId(payload.getInstitutionId());
        entity.setInstitutionName(payload.getInstitutionName());
        entity.setInstitutionRootName(payload.getInstitutionRootName());
        entity.setInstitutionTaxCode(payload.getInstitutionTaxCode());
        entity.setInstitutionType(payload.getInstitutionType());
        entity.setPtName(payload.getPtName());
        entity.setPtTaxCode(payload.getPtTaxCode());
        entity.setPtType(payload.getPtType());
        return entity;
    }
}
