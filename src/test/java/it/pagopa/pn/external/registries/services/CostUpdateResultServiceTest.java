package it.pagopa.pn.external.registries.services;

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
import java.util.UUID;

import static org.mockito.Mockito.*;
class CostUpdateResultServiceTest {
    @Mock
    private CostUpdateResultDao dao;

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    private CostUpdateResultService service;

    @Captor
    private ArgumentCaptor<CostUpdateResultEntity> captor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.service = new CostUpdateResultService(dao, communicationResultGroupMapper);
        captor = ArgumentCaptor.forClass(CostUpdateResultEntity.class);
    }

    @Test
    void checkCleanUp() {
        int statusCode = 200;

        String sourceJsonString = "{\"iuv\":\"iuv\",\"organizationFiscalCode\":null,\"amount\":100,\"description\":\"description\",\"isPartialPayment\":null,\"dueDate\":null,\"retentionDate\":null,\"paymentDate\":null,\"reportingDate\":null,\"insertedDate\":null,\"paymentMethod\":null,\"fee\":null,\"notificationFee\":null,\"pspCompany\":null,\"idReceipt\":null,\"idFlowReporting\":null,\"status\":null,\"lastUpdatedDate\":null,\"transfer\":[{\"amount\":180, \"remittanceInformation\":\"remittanceInformation\"}]}";
        String cleanedJsonString = "{\"iuv\":\"iuv\",\"amount\":100,\"description\":\"description\",\"transfer\":[{\"amount\":180}]}";

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "OK" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        String result = service.createUpdateResult(request).block();

        verify(dao).insertOrUpdate(captor.capture());
        CostUpdateResultEntity capturedEntity = captor.getValue();

        // communicationResult
        Assertions.assertNotNull(capturedEntity.getCommunicationResult());
        Assertions.assertEquals(cleanedJsonString, capturedEntity.getCommunicationResult().getJsonResponse());
    }

    @Test
    void testCreateUpdateResult_200_OK() {
        int statusCode = 200;

        String sourceJsonString = "{\"iuv\":\"iuv\",\"organizationFiscalCode\":null,\"amount\":100,\"description\":\"description\",\"isPartialPayment\":null,\"dueDate\":null,\"retentionDate\":null,\"paymentDate\":null,\"reportingDate\":null,\"insertedDate\":null,\"paymentMethod\":null,\"fee\":null,\"notificationFee\":null,\"pspCompany\":null,\"idReceipt\":null,\"idFlowReporting\":null,\"status\":null,\"lastUpdatedDate\":null,\"transfer\":[]}";
        String cleanedJsonString = "{\"iuv\":\"iuv\",\"amount\":100,\"description\":\"description\",\"transfer\":[]}";

        // Setup request
        CostUpdateResultRequestInt request = newCostUpdateResultRequestInt(statusCode, sourceJsonString);

        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                "OK" + "##" +
                UUID.randomUUID());

        when(dao.insertOrUpdate(any())).thenReturn(Mono.just(entity));

        // Execute & Verify
        String result = service.createUpdateResult(request).block();
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
        Assertions.assertEquals(cleanedJsonString, capturedEntity.getCommunicationResult().getJsonResponse());

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
