package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.mapper.PtDelegationPayloadToPtDelegationEntity;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.dao.PtDelegationsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID;

@Service
@CustomLog
@RequiredArgsConstructor
public class PtDelegationService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(PtDelegationEntity.STATUS_ACTIVE, PtDelegationEntity.STATUS_DELETED);

    private final PtDelegationsDao ptDelegationsDao;
    private final OnboardInstitutionsDao onboardInstitutionsDao;
    private final Clock clock;

    public Mono<Boolean> saveDelegationEvent(PnPtDelegationPayload payload) {
        return Mono.fromRunnable(() -> validate(payload))
                .then(Mono.defer(() -> checkInstitutionOnboarded(payload.getInstitutionId())))
                .then(Mono.defer(() -> {
                    Instant lastChangeAt = computeLastChangeAt(payload);
                    PtDelegationEntity entity = PtDelegationPayloadToPtDelegationEntity.toEntity(payload, lastChangeAt);
                    return ptDelegationsDao.insertIfAbsent(entity);
                }))
                .doOnNext(inserted -> log.info("Delegation event handled delegationId={} ptId={} institutionId={} status={} inserted={}",
                        payload.getDelegationId(), payload.getPtId(), payload.getInstitutionId(), payload.getStatus(), inserted));
    }

    private Mono<OnboardInstitutionEntity> checkInstitutionOnboarded(String institutionId) {
        return onboardInstitutionsDao.get(institutionId)
                .filter(OnboardInstitutionEntity::isActive)
                .switchIfEmpty(Mono.error(() -> new PnInternalException("Institution not onboarded institutionId=" + institutionId, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED)));
    }

    Instant computeLastChangeAt(PnPtDelegationPayload payload) {
        if (payload.getUpdatedAt() != null) {
            return payload.getUpdatedAt();
        }
        if (payload.getCreatedAt() != null) {
            return payload.getCreatedAt();
        }
        return Instant.now(clock);
    }

    private void validate(PnPtDelegationPayload payload) {
        if (payload == null) {
            throw new PnInternalException("Delegation payload is null", ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID);
        }

        List<String> missingFields = new ArrayList<>();
        requireText(missingFields, "delegationId", payload.getDelegationId());
        requireText(missingFields, "delegationType", payload.getDelegationType());
        requireText(missingFields, "productId", payload.getProductId());
        requireText(missingFields, "status", payload.getStatus());
        requireText(missingFields, "sourceEventId", payload.getSourceEventId());
        requireText(missingFields, "institutionId", payload.getInstitutionId());
        requireText(missingFields, "institutionName", payload.getInstitutionName());
        requireText(missingFields, "institutionTaxCode", payload.getInstitutionTaxCode());
        requireText(missingFields, "institutionType", payload.getInstitutionType());
        requireText(missingFields, "ptId", payload.getPtId());
        requireText(missingFields, "ptName", payload.getPtName());
        requireText(missingFields, "ptTaxCode", payload.getPtTaxCode());
        requireText(missingFields, "ptType", payload.getPtType());
        if (payload.getCreatedAt() == null) {
            missingFields.add("createdAt");
        }

        if (!missingFields.isEmpty()) {
            throw new PnInternalException("Invalid delegation payload delegationId=" + payload.getDelegationId() + " missingFields=" + missingFields, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID);
        }

        if (!ALLOWED_STATUSES.contains(payload.getStatus())) {
            throw new PnInternalException("Invalid delegation payload delegationId=" + payload.getDelegationId() + " status=" + payload.getStatus(), ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID);
        }
    }

    private static void requireText(List<String> missingFields, String fieldName, String value) {
        if (StringUtils.isBlank(value)) {
            missingFields.add(fieldName);
        }
    }
}
