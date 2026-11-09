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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED;

@Service
@CustomLog
@RequiredArgsConstructor
public class PtDelegationService {

    private final PtDelegationsDao ptDelegationsDao;
    private final OnboardInstitutionsDao onboardInstitutionsDao;
    private final Clock clock;

    public Mono<Boolean> saveDelegationEvent(PnPtDelegationPayload payload, Instant sentAt) {
        return Mono.defer(() -> checkInstitutionOnboarded(payload.getInstitutionId()))
                .then(Mono.defer(() -> {
                    Instant lastChangeAt = computeLastChangeAt(payload, sentAt);
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

    Instant computeLastChangeAt(PnPtDelegationPayload payload, Instant sentAt) {
        if (payload.getUpdatedAt() != null) {
            return payload.getUpdatedAt();
        }
        if (payload.getCreatedAt() != null) {
            return payload.getCreatedAt();
        }
        if (sentAt != null) {
            return sentAt;
        }
        return Instant.now(clock);
    }

}
