package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaymentForRecipientInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class CostUpdateOrchestratorServiceIT {

    @Autowired
    private CostComponentsDao costComponentsDao;

    @Autowired
    private CostUpdateResultDao costUpdateResultDao;

    @Mock
    private GpdClient gpdClient;

    private CostUpdateOrchestratorService costUpdateOrchestratorService;

    private final CostComponentsMapper costComponentsMapper = new CostComponentsMapper();

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<CostComponentsEntity> costComponentsEntityTestDao;

    private final String iun = "iun";
    private final int recIndex = 0;
    private final String creditorTaxId = "testTaxId";
    private final String noticeCode = "testNoticeCode";

    private final String costComponentEntityPk = iun + "##" + recIndex;
    private final String costComponentEntitySk = creditorTaxId + "##" + noticeCode;

    private final int baseCost = 100;
    private final int notificationStepCost = 200;
    private final Integer vat = 22;

    private CostComponentService costComponentService; // used for checking what's sent to GPD

    @BeforeEach
    void setUp() {
        CostUpdateResultService costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        UpdateCostService updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
        this.costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
        costUpdateOrchestratorService = new CostUpdateOrchestratorService(costComponentService, updateCostService);

        costComponentsEntityTestDao = new TestDao<>(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameCostComponents(), CostComponentsEntity.class);
    }

    @Test
    void VALIDATION_then_SEND_SIMPLE_REGISTERED_LETTER_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // SEND_SIMPLE_REGISTERED_LETTER - update

        // When - handler call
        result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to baseCost + notificationStepCost
        costComponentService.getTotalCost(vat, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(344, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void VALIDATION_then_SEND_ANALOG_DOMICILE_ATTEMPT_0_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // SEND_ANALOG_DOMICILE_ATTEMPT_0 - update

        // When - handler call
        result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to baseCost + notificationStepCost
        costComponentService.getTotalCost(vat, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(344, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void VALIDATION_then_SEND_ANALOG_DOMICILE_ATTEMPT_1_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // SEND_ANALOG_DOMICILE_ATTEMPT_1 - update

        // When - handler call
        result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_1
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to baseCost + notificationStepCost
        costComponentService.getTotalCost(vat, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(344, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void VALIDATION_then_SEND_ANALOG_DOMICILE_ATTEMPT_0_ATTEMPT_1_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // SEND_ANALOG_DOMICILE_ATTEMPT_0 - update

        // When - handler call
        result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // SEND_ANALOG_DOMICILE_ATTEMPT_1 - update

        // When - handler call
        result = costUpdateOrchestratorService.handleCostUpdateForIun(
                vat,
                notificationStepCost,
                iun,
                recIndex,
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_1
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to baseCost + notificationStepCost + notificationStepCost
        
        costComponentService.getTotalCost(vat, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(588, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void VALIDATION_then_REQUEST_REFUSED_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // REQUEST_REFUSED - update

        // When - webservice call
        result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.REQUEST_REFUSED
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to 0
        costComponentService.getTotalCost(null, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(0, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void VALIDATION_then_NOTIFICATION_CANCELLED_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // VALIDATION - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.VALIDATION
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // NOTIFICATION_CANCELLED - update

        // When - webservice call
        result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.NOTIFICATION_CANCELLED
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to 0
        costComponentService.getTotalCost(null, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(0, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
    }

    @Test
    void REQUEST_REFUSED_Alone_Success() {
        // Given
        Instant eventTimestamp = Instant.now();
        Instant eventStorageTimestamp = eventTimestamp.plusSeconds(1);

        // mock gpdClient
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));

        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID

        // REQUEST_REFUSED - first insert

        // When - webservice call
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
                null,
                baseCost,
                iun,
                new PaymentForRecipientInt[] { new PaymentForRecipientInt(recIndex, creditorTaxId, noticeCode) },
                eventTimestamp,
                eventStorageTimestamp,
                CostUpdateCostPhaseInt.REQUEST_REFUSED
        ).collectList().block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getRecIndex());
        Assertions.assertEquals(creditorTaxId, result.get(0).getCreditorTaxId());
        Assertions.assertEquals(noticeCode, result.get(0).getNoticeCode());
        Assertions.assertEquals(CommunicationResultGroupInt.OK, result.get(0).getResult());

        // check that costComponentService.getTotalCost corresponds to 0
        costComponentService.getTotalCost(null, iun, recIndex, creditorTaxId, noticeCode)
                .doOnNext(totalCost -> Assertions.assertEquals(0, totalCost))
                .block();

        // Clean-up
        try {
            costComponentsEntityTestDao.delete(costComponentEntityPk, costComponentEntitySk);
        }
        catch (Exception e) {
            System.out.println("Nothing to remove for costComponentsEntityTestDao");
        }
        // no remove for costUpdateEntityTestDao, random UUID
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
