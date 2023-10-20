package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultRequestInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;
class CostUpdateResultServiceTest {
    @Mock
    private CostUpdateResultDao dao;

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    private CostUpdateResultService service;

    @Captor
    private ArgumentCaptor<CostUpdateResultEntity> captor;

    String sourceJsonString;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.service = new CostUpdateResultService(dao, communicationResultGroupMapper);
        this.captor = ArgumentCaptor.forClass(CostUpdateResultEntity.class);

        sourceJsonString = "{\"iuv\":\"iuv\",\"organizationFiscalCode\":null,\"amount\":100,\"description\":\"description\",\"isPartialPayment\":null,\"dueDate\":null,\"retentionDate\":null,\"paymentDate\":null,\"reportingDate\":null,\"insertedDate\":null,\"paymentMethod\":null,\"fee\":null,\"notificationFee\":null,\"pspCompany\":null,\"idReceipt\":null,\"idFlowReporting\":null,\"status\":null,\"lastUpdatedDate\":null,\"transfer\":[]}";
    }

    @Test
    void testCreateUpdateResult_200_OK() {
        int statusCode = 200;

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "OK" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        String result = Objects.requireNonNull(service.createUpdateResult(request).block()).getValue();
        Assertions.assertEquals("OK", result);

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // pk
        Assertions.assertEquals(request.getCreditorTaxId() + "##" + request.getNoticeCode(), capturedEntity.getPk());

        // sk - we want to check that the SK is composed by the updateCostPhase, the communicationResultGroup, but ignore the final random UUID
        String expectedSkPrefix = request.getUpdateCostPhase().getValue() + "##" + "OK";
        String actualSkPrefix = capturedEntity.getSk().split("##")[0] + "##" + capturedEntity.getSk().split("##")[1];
        Assertions.assertEquals(expectedSkPrefix, actualSkPrefix);

        // requestId
        Assertions.assertEquals(request.getRequestId(), capturedEntity.getRequestId());

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals("OK_UPDATED", capturedEntity.getCommunicationResult().getResultEnum());
        Assertions.assertEquals(statusCode, capturedEntity.getCommunicationResult().getStatusCode());
        Assertions.assertEquals(sourceJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

        // communicationResultGroup
        Assertions.assertEquals("OK", capturedEntity.getCommunicationResultGroup());

        // failedIuv
        Assertions.assertNull(capturedEntity.getFailedIuv());

        // updateCostPhase
        Assertions.assertEquals(request.getUpdateCostPhase().getValue(), capturedEntity.getUpdateCostPhase());

        // notificationCost
        Assertions.assertEquals(request.getNotificationCost(), capturedEntity.getNotificationCost());

        // iun
        Assertions.assertEquals(request.getIun(), capturedEntity.getIun());

        // eventTimestamp
        Assertions.assertEquals(request.getEventTimestamp(), capturedEntity.getEventTimestamp());
        // eventStorageTimestamp
        Assertions.assertEquals(request.getEventStorageTimestamp(), capturedEntity.getEventStorageTimestamp());
        // communicationTimestamp
        Assertions.assertEquals(request.getCommunicationTimestamp(), capturedEntity.getCommunicationTimestamp());
    }

    @Test
    void testCreateUpdateResult_209_OK_IN_PAYMENT_Reduced() {
        int statusCode = 209;

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "OK" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        CommunicationResultGroupInt result = service.createUpdateResult(request).block();
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result);

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // pk
        Assertions.assertEquals(request.getCreditorTaxId() + "##" + request.getNoticeCode(), capturedEntity.getPk());

        // sk - we want to check that the SK is composed by the updateCostPhase, the communicationResultGroup, but ignore the final random UUID
        String expectedSkPrefix = request.getUpdateCostPhase().getValue() + "##" + "OK";
        String actualSkPrefix = capturedEntity.getSk().split("##")[0] + "##" + capturedEntity.getSk().split("##")[1];
        Assertions.assertEquals(expectedSkPrefix, actualSkPrefix);

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals("OK_IN_PAYMENT", capturedEntity.getCommunicationResult().getResultEnum());
        Assertions.assertEquals(statusCode, capturedEntity.getCommunicationResult().getStatusCode());
        Assertions.assertEquals(sourceJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

        // communicationResultGroup
        Assertions.assertEquals("OK", capturedEntity.getCommunicationResultGroup());

        // failedIuv
        Assertions.assertNull(capturedEntity.getFailedIuv());
    }

    @Test
    void testCreateUpdateResult_404_KO_NOT_FOUND_Reduced() {
        int statusCode = 404;

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "KO" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        CommunicationResultGroupInt result = service.createUpdateResult(request).block();
        Assertions.assertEquals(CommunicationResultGroupInt.KO, result);

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // pk
        Assertions.assertEquals(request.getCreditorTaxId() + "##" + request.getNoticeCode(), capturedEntity.getPk());

        // sk - we want to check that the SK is composed by the updateCostPhase, the communicationResultGroup, but ignore the final random UUID
        String expectedSkPrefix = request.getUpdateCostPhase().getValue() + "##" + "KO";
        String actualSkPrefix = capturedEntity.getSk().split("##")[0] + "##" + capturedEntity.getSk().split("##")[1];
        Assertions.assertEquals(expectedSkPrefix, actualSkPrefix);

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals("KO_NOT_FOUND", capturedEntity.getCommunicationResult().getResultEnum());
        Assertions.assertEquals(statusCode, capturedEntity.getCommunicationResult().getStatusCode());
        Assertions.assertEquals(sourceJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

        // communicationResultGroup
        Assertions.assertEquals("KO", capturedEntity.getCommunicationResultGroup());

        // failedIuv
        Assertions.assertEquals(request.getIun(), capturedEntity.getFailedIuv());
    }

    @Test
    void testCreateUpdateResult_422_KO_CANNOT_UPDATE_Reduced() {
        int statusCode = 422;

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "KO" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        CommunicationResultGroupInt result = service.createUpdateResult(request).block();
        Assertions.assertEquals(CommunicationResultGroupInt.KO, result);

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // pk
        Assertions.assertEquals(request.getCreditorTaxId() + "##" + request.getNoticeCode(), capturedEntity.getPk());

        // sk - we want to check that the SK is composed by the updateCostPhase, the communicationResultGroup, but ignore the final random UUID
        String expectedSkPrefix = request.getUpdateCostPhase().getValue() + "##" + "KO";
        String actualSkPrefix = capturedEntity.getSk().split("##")[0] + "##" + capturedEntity.getSk().split("##")[1];
        Assertions.assertEquals(expectedSkPrefix, actualSkPrefix);

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals("KO_CANNOT_UPDATE", capturedEntity.getCommunicationResult().getResultEnum());
        Assertions.assertEquals(statusCode, capturedEntity.getCommunicationResult().getStatusCode());
        Assertions.assertEquals(sourceJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

        // communicationResultGroup
        Assertions.assertEquals("KO", capturedEntity.getCommunicationResultGroup());

        // failedIuv
        Assertions.assertEquals(request.getIun(), capturedEntity.getFailedIuv());
    }

    @Test
    void testCreateUpdateResult_500_KO_RETRY_Reduced() {
        int statusCode = 500;

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "RETRY" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        CommunicationResultGroupInt result = service.createUpdateResult(request).block();
        Assertions.assertEquals(CommunicationResultGroupInt.RETRY, result);

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // pk
        Assertions.assertEquals(request.getCreditorTaxId() + "##" + request.getNoticeCode(), capturedEntity.getPk());

        // sk - we want to check that the SK is composed by the updateCostPhase, the communicationResultGroup, but ignore the final random UUID
        String expectedSkPrefix = request.getUpdateCostPhase().getValue() + "##" + "RETRY";
        String actualSkPrefix = capturedEntity.getSk().split("##")[0] + "##" + capturedEntity.getSk().split("##")[1];
        Assertions.assertEquals(expectedSkPrefix, actualSkPrefix);

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals("KO_RETRY", capturedEntity.getCommunicationResult().getResultEnum());
        Assertions.assertEquals(statusCode, capturedEntity.getCommunicationResult().getStatusCode());
        Assertions.assertEquals(sourceJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

        // communicationResultGroup
        Assertions.assertEquals("RETRY", capturedEntity.getCommunicationResultGroup());

        // failedIuv
        Assertions.assertNull(capturedEntity.getFailedIuv());
    }

    @Test
    void testNullRequest() {
        final Mono<CommunicationResultGroupInt> updateResult = service.createUpdateResult(null);

        // Execute & Verify
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            updateResult.block();
        });
    }

    private CostUpdateResultRequestInt newCostUpdateResultRequestInt(int statusCode, String sourceJsonString) {
        CostUpdateResultRequestInt request = new CostUpdateResultRequestInt();
        request.setCreditorTaxId("1234");
        request.setNoticeCode("NOTICE123");
        request.setUpdateCostPhase(CostUpdateCostPhaseInt.VALIDATION);
        request.setRequestId("REQ123");
        request.setStatusCode(statusCode);
        request.setJsonResponse(sourceJsonString);
        request.setNotificationCost(100);
        request.setIun("IUN123");
        var now = Instant.now();
        request.setEventTimestamp(Instant.now());
        request.setEventStorageTimestamp(now.plusSeconds(1));
        request.setCommunicationTimestamp(now.plusSeconds(5));

        return request;
    }
}
