package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.dao.PtDelegationsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PtDelegationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");
    private static final Instant CREATED_AT = Instant.parse("2026-11-02T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-11-20T09:30:00.123456Z");

    @Mock
    private PtDelegationsDao ptDelegationsDao;

    @Mock
    private OnboardInstitutionsDao onboardInstitutionsDao;

    private PtDelegationService ptDelegationService;

    @BeforeEach
    void setup() {
        ptDelegationService = new PtDelegationService(ptDelegationsDao, onboardInstitutionsDao, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void saveDelegationEventInsertsEvent() {
        PnPtDelegationPayload payload = payload();
        payload.setUpdatedAt(UPDATED_AT);
        when(onboardInstitutionsDao.get("ente-A")).thenReturn(Mono.just(onboardInstitution(OnboardInstitutionEntity.STATUS_ACTIVE)));
        when(ptDelegationsDao.insertIfAbsent(any())).thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectNext(Boolean.TRUE)
                .verifyComplete();

        ArgumentCaptor<PtDelegationEntity> captor = ArgumentCaptor.forClass(PtDelegationEntity.class);
        verify(ptDelegationsDao).insertIfAbsent(captor.capture());
        PtDelegationEntity entity = captor.getValue();
        assertThat(entity.getPtId()).isEqualTo("pt-1");
        assertThat(entity.getEventKey()).isEqualTo("ente-A#2026-11-20T09:30:00.123456Z");
        assertThat(entity.getLastChangeAt()).isEqualTo(UPDATED_AT);
        assertThat(entity.getDelegationId()).isEqualTo("delegation-1");
        assertThat(entity.getStatus()).isEqualTo(PtDelegationEntity.STATUS_ACTIVE);
        assertThat(entity.getInstitutionId()).isEqualTo("ente-A");
    }

    @Test
    void saveDelegationEventDuplicatedIsNotInsertedAgain() {
        when(onboardInstitutionsDao.get("ente-A")).thenReturn(Mono.just(onboardInstitution(OnboardInstitutionEntity.STATUS_ACTIVE)));
        when(ptDelegationsDao.insertIfAbsent(any())).thenReturn(Mono.just(Boolean.FALSE));

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload()))
                .expectNext(Boolean.FALSE)
                .verifyComplete();
    }

    @Test
    void saveDelegationEventFailsWhenInstitutionNotFound() {
        when(onboardInstitutionsDao.get("ente-A")).thenReturn(Mono.empty());

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload()))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED))
                .verify();

        verifyNoInteractions(ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenInstitutionNotActive() {
        when(onboardInstitutionsDao.get("ente-A")).thenReturn(Mono.just(onboardInstitution(OnboardInstitutionEntity.STATUS_CLOSED)));

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload()))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED))
                .verify();

        verifyNoInteractions(ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenRequiredFieldMissing() {
        PnPtDelegationPayload payload = payload();
        payload.setPtId(null);
        payload.setInstitutionTaxCode(" ");

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectErrorSatisfies(ex -> {
                    assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID);
                    assertThat(((PnInternalException) ex).getProblem().getDetail()).contains("ptId").contains("institutionTaxCode");
                })
                .verify();

        verifyNoInteractions(onboardInstitutionsDao, ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenDelegationIdMissing() {
        PnPtDelegationPayload payload = payload();
        payload.setDelegationId(null);

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID))
                .verify();

        verifyNoInteractions(onboardInstitutionsDao, ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenStatusMissing() {
        PnPtDelegationPayload payload = payload();
        payload.setStatus(null);

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID))
                .verify();

        verifyNoInteractions(onboardInstitutionsDao, ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenCreatedAtMissing() {
        PnPtDelegationPayload payload = payload();
        payload.setCreatedAt(null);

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectErrorSatisfies(ex -> {
                    assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID);
                    assertThat(((PnInternalException) ex).getProblem().getDetail()).contains("createdAt");
                })
                .verify();

        verifyNoInteractions(onboardInstitutionsDao, ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenStatusNotAllowed() {
        PnPtDelegationPayload payload = payload();
        payload.setStatus("SUSPENDED");

        StepVerifier.create(ptDelegationService.saveDelegationEvent(payload))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID))
                .verify();

        verifyNoInteractions(onboardInstitutionsDao, ptDelegationsDao);
    }

    @Test
    void saveDelegationEventFailsWhenPayloadNull() {
        StepVerifier.create(ptDelegationService.saveDelegationEvent(null))
                .expectErrorSatisfies(ex -> assertErrorCode(ex, ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INVALID))
                .verify();
    }

    @Test
    void computeLastChangeAtUsesUpdatedAt() {
        PnPtDelegationPayload payload = payload();
        payload.setUpdatedAt(UPDATED_AT);

        assertThat(ptDelegationService.computeLastChangeAt(payload)).isEqualTo(UPDATED_AT);
    }

    @Test
    void computeLastChangeAtFallsBackToCreatedAt() {
        assertThat(ptDelegationService.computeLastChangeAt(payload())).isEqualTo(CREATED_AT);
    }

    @Test
    void computeLastChangeAtFallsBackToReceptionDate() {
        PnPtDelegationPayload payload = payload();
        payload.setCreatedAt(null);

        assertThat(ptDelegationService.computeLastChangeAt(payload)).isEqualTo(NOW);
    }

    private static void assertErrorCode(Throwable ex, String errorCode) {
        assertThat(ex).isInstanceOf(PnInternalException.class);
        assertThat(((PnInternalException) ex).getProblem().getErrors().get(0).getCode()).isEqualTo(errorCode);
    }

    private static OnboardInstitutionEntity onboardInstitution(String status) {
        OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
        entity.setPk("ente-A");
        entity.setStatus(status);
        return entity;
    }

    private static PnPtDelegationPayload payload() {
        PnPtDelegationPayload payload = new PnPtDelegationPayload();
        payload.setDelegationId("delegation-1");
        payload.setDelegationType("PT");
        payload.setProductId("prod-pn");
        payload.setStatus(PtDelegationEntity.STATUS_ACTIVE);
        payload.setIsTest(false);
        payload.setSourceEventId("source-event-1");
        payload.setSourceEventType("ADD");
        payload.setCreatedAt(CREATED_AT);
        payload.setInstitutionId("ente-A");
        payload.setInstitutionName("Comune di Test");
        payload.setInstitutionTaxCode("01199250158");
        payload.setInstitutionType("PA");
        payload.setPtId("pt-1");
        payload.setPtName("Partner Tecnologico");
        payload.setPtTaxCode("12345678901");
        payload.setPtType("PT");
        return payload;
    }
}
