package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CostUpdateOrchestratorServiceTest {
    private CostUpdateOrchestratorService costUpdateOrchestratorService;

    @Mock
    private CostUpdateResultDao costUpdateResultDao;
    @Mock
    private CostComponentsDao costComponentsDao;

    @Mock
    private GpdClient gpdClient;

    private final CostComponentsMapper costComponentsMapper = new CostComponentsMapper();

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    private final String iun = "iun";
    private final int recIndex = 0;
    private final String creditorTaxId = "testTaxId";
    private final String noticeCode = "testNoticeCode";

    private final String costComponentEntityPk = iun + "##" + recIndex;
    private final String costComponentEntitySk = creditorTaxId + "##" + noticeCode;

    private final int baseCost = 100;
    private final int notificationStepCost = 100;
    private Integer vat = 22;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        CostComponentService costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
        CostUpdateResultService costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        UpdateCostService updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
        this.costUpdateOrchestratorService = new CostUpdateOrchestratorService(costComponentService, updateCostService);
    }

    @Test
    void handleCostUpdateForIun_SEND_SIMPLE_REGISTERED_LETTER_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER;

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItems(any()))
                .thenReturn(Flux.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                updateCostPhase
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        Assertions.assertEquals(recIndex, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // Verify called methods
        verify(costComponentsDao, times(1)).getItems(any());
        verify(costComponentsDao, times(1)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(2)).getItemStrong(any(), any());
        verify(gpdClient, times(1)).setNotificationCost(any(), any(), any(), any());
    }


    @Test
    void handleCostUpdateForIun_SEND_SIMPLE_REGISTERED_LETTER_NotExistCostItem() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER;

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        //when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.empty());
        when(costComponentsDao.getItems(any()))
                .thenReturn(Flux.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                updateCostPhase
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        System.out.println("result: "+result);
        Assertions.assertEquals(1, result.size());

        Assertions.assertEquals(recIndex, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // Verify called methods
        verify(costComponentsDao, times(1)).getItems(any());
        verify(costComponentsDao, times(0)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(1)).getItemStrong(any(), any());
        verify(gpdClient, times(0)).setNotificationCost(any(), any(), any(), any());
    }

    @Test
    void handleCostUpdateForIun_SEND_SIMPLE_REGISTERED_LETTER_GetItemsFailure() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER;

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItems(any()))
                .thenReturn(Flux.error(new RuntimeException()));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        try {
            costUpdateOrchestratorService.handleCostUpdateForIun(
                    vat,
                    notificationStepCost,
                    iun,
                    recIndex,
                    eventTimestamp,
                    eventStorageTimestamp,
                    updateCostPhase
            ).collectList().block();
            Assertions.fail("We should get an exception");
        } catch (Exception e) {
            // Then
            Assertions.assertEquals("java.lang.RuntimeException", e.getClass().getName());
        }

        // Verify called methods
        verify(costComponentsDao, times(1)).getItems(any());
        verify(costComponentsDao, times(0)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(0)).getItemStrong(any(), any());
        verify(gpdClient, times(0)).setNotificationCost(any(), any(), any(), any());
    }

    @Test
    void handleCostUpdateForIuvs_VALIDATION_Success() {
        // Given
        PaymentForRecipientInt paymentForRecipient = new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode);
        PaymentForRecipientInt[] paymentsForRecipients = {paymentForRecipient};
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.VALIDATION; // performs an insert

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                null
        );
        when(costComponentsDao.insertOrUpdate(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                notificationStepCost,
                iun,
                paymentsForRecipients,
                eventTimestamp,
                eventStorageTimestamp,
                updateCostPhase
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // Verify called methods
        verify(costComponentsDao, times(1)).insertOrUpdate(any());
        verify(costComponentsDao, times(0)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(2)).getItemStrong(any(), any());
        verify(gpdClient, times(1)).setNotificationCost(any(), any(), any(), any());
        verify(costUpdateResultDao, times(1)).insertOrUpdate(any());
    }

    @Test
    void handleCostUpdateForIuvs_SEND_SIMPLE_REGISTERED_LETTER_Success() {
        // Given
        PaymentForRecipientInt paymentForRecipient = new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode);
        PaymentForRecipientInt[] paymentsForRecipients = {paymentForRecipient, paymentForRecipient};
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER; // performs an update

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                vat,
                notificationStepCost,
                iun,
                paymentsForRecipients,
                eventTimestamp,
                eventStorageTimestamp,
                updateCostPhase
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        Assertions.assertEquals(0, result.get(1).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(1).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(1).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(1).getResult());

        // Verify called methods
        verify(costComponentsDao, times(0)).insertOrUpdate(any());
        verify(costComponentsDao, times(2)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(4)).getItemStrong(any(), any());
        verify(gpdClient, times(2)).setNotificationCost(any(), any(), any(), any());
        verify(costUpdateResultDao, times(2)).insertOrUpdate(any());
    }

    @Test
    void handleCostUpdateForIuvs_SEND_SIMPLE_REGISTERED_LETTER_InsertFailure() {
        // it must be like the test before, but have an exception on insertStep operation

        // Given
        PaymentForRecipientInt paymentForRecipient = new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode);
        PaymentForRecipientInt[] paymentsForRecipients = {paymentForRecipient};
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER; // performs an update

        // mock gpdClient (we shouldn't call it)
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        // we want to throw an exception on insertStepCost
        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.error(new RuntimeException()));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));

        // mock costUpdateResultDao (we should not call it)
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        try {
            costUpdateOrchestratorService.handleCostUpdateForIuvs(
                    vat,
                    notificationStepCost,
                    iun,
                    paymentsForRecipients,
                    eventTimestamp,
                    eventStorageTimestamp,
                    updateCostPhase
            ).collectList().block();
            Assertions.fail("We should get an exception");
        } catch (Exception e) {
            // Then
            Assertions.assertEquals("java.lang.RuntimeException", e.getClass().getName());
        }

        // Verify called methods
        verify(costComponentsDao, times(0)).insertOrUpdate(any());
        verify(costComponentsDao, times(1)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(1)).getItemStrong(any(), any());
        verify(gpdClient, times(0)).setNotificationCost(any(), any(), any(), any());
        verify(costUpdateResultDao, times(0)).insertOrUpdate(any());
    }

    @Test
    void handleCostUpdateForIuvs_SEND_SIMPLE_REGISTERED_LETTER_GetFailure() {
        // Given
        PaymentForRecipientInt paymentForRecipient = new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode);
        PaymentForRecipientInt[] paymentsForRecipients = {paymentForRecipient};
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER; // performs an update

        // mock gpdClient (we shouldn't call it)
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );

        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        // we want to throw an exception on getTotalCost
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.error(new RuntimeException()));

        // mock costUpdateResultDao (we should not call it)
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        try {
            costUpdateOrchestratorService.handleCostUpdateForIuvs(
                    vat,
                    notificationStepCost,
                    iun,
                    paymentsForRecipients,
                    eventTimestamp,
                    eventStorageTimestamp,
                    updateCostPhase
            ).collectList().block();
            Assertions.fail("We should get an exception");
        } catch (Exception e) {
            // Then
            Assertions.assertEquals("java.lang.RuntimeException", e.getClass().getName());
        }

        // Verify called methods
        verify(costComponentsDao, times(0)).insertOrUpdate(any());
        verify(costComponentsDao, times(0)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(1)).getItemStrong(any(), any());
        verify(gpdClient, times(0)).setNotificationCost(any(), any(), any(), any());
        verify(costUpdateResultDao, times(0)).insertOrUpdate(any());
    }

    @Test
    void handleCostUpdateForIuvs_SEND_SIMPLE_REGISTERED_LETTER_GpdFailure() {
        // Given
        PaymentForRecipientInt paymentForRecipient = new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode);
        PaymentForRecipientInt[] paymentsForRecipients = {paymentForRecipient};
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);
        CostUpdateCostPhaseInt updateCostPhase = CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER; // performs an update

        // mock gpdClient
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.error(new RuntimeException()));

        // mock costComponentsDao
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity(
                costComponentEntityPk,
                costComponentEntitySk,
                baseCost,
                notificationStepCost,
                0,
                0,
                false,
                vat
        );
        when(costComponentsDao.updateNotNullIfExists(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItemStrong(any(), any())).thenReturn(Mono.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        try {
            costUpdateOrchestratorService.handleCostUpdateForIuvs(
                    vat,
                    notificationStepCost,
                    iun,
                    paymentsForRecipients,
                    eventTimestamp,
                    eventStorageTimestamp,
                    updateCostPhase
            ).collectList().block();
            Assertions.fail("We should get an exception");
        } catch (Exception e) {
            // Then
            Assertions.assertEquals("java.lang.RuntimeException", e.getClass().getName());
        }

        // Verify called methods
        verify(costComponentsDao, times(0)).insertOrUpdate(any());
        verify(costComponentsDao, times(1)).updateNotNullIfExists(any());
        verify(costComponentsDao, times(2)).getItemStrong(any(), any());
        verify(gpdClient, times(1)).setNotificationCost(any(), any(), any(), any());
        verify(costUpdateResultDao, times(0)).insertOrUpdate(any());
    }

    private PaymentsModelResponse newPaymentModelResponse() {
        return new PaymentsModelResponse()
                .iuv(iun)
                .organizationFiscalCode(creditorTaxId)
                .amount((long)notificationStepCost)
                .status(PaymentsModelResponse.StatusEnum.PAID)
                .lastUpdatedDate(new Date());
    }
}
