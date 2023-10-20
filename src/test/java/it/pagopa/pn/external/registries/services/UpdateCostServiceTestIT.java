package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class UpdateCostServiceTestIT {

    private UpdateCostService updateCostService;

    @Mock
    private CostUpdateResultDao costUpdateResultDao; // DynamoDB DAO

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    @Mock
    private GpdClient gpdClient; // GPD remote service

    private final int recIndex = 0;
    private final String creditorTaxId = "testTaxId";
    private final String noticeCode = "testNoticeCode";

    private final String pk = "testPk";
    private final String sk = "testSk";

    private final int notificationCost = 100;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CostUpdateResultService costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
    }

    @Test
    void testUpdateCost_200_OK() {
        // Given
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();

        // GPD client returns a successful response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                recIndex, creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(recIndex, updateCostResponse.getRecIndex(), "RecIndex should match");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.OK, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    @Test
    void testUpdateCost_209_OK() {
        int status = 209;

        // Given
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();

        // GPD client returns a successful response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.status(status).body(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                recIndex, creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(recIndex, updateCostResponse.getRecIndex(), "RecIndex should match");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.OK, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    @Test
    void testUpdateCost_404_KO() {
        int status = 404;

        // Given
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();

        // GPD client returns an error response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.status(status).body(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any()))
                .thenReturn(Mono.error(new WebClientResponseException("An error occurred", status, "Not found", null, null, null)));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                recIndex, creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(recIndex, updateCostResponse.getRecIndex(), "RecIndex should match");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.KO, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    @Test
    void testUpdateCost_422_KO() {
        int status = 422;

        // Given
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();

        // GPD client returns an error response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.status(status).body(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any()))
                .thenReturn(Mono.error(new WebClientResponseException("An error occurred", status, "Can't update", null, null, null)));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                recIndex, creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(recIndex, updateCostResponse.getRecIndex(), "RecIndex should match");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.KO, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    @Test
    void testUpdateCost_500_RETRY() {
        int status = 500;

        // Given
        PaymentsModelResponse paymentsModelResponse = newPaymentModelResponse();

        // GPD client returns an error response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.status(status).body(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any()))
                .thenReturn(Mono.error(new WebClientResponseException("An error occurred", status, "Internal Server Error", null, null, null)));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                recIndex, creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(recIndex, updateCostResponse.getRecIndex(), "RecIndex should match");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.RETRY, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    private PaymentsModelResponse newPaymentModelResponse() {
        String iun = "iun";

        return new PaymentsModelResponse()
                .iuv(iun)
                .organizationFiscalCode(creditorTaxId)
                .amount((long)notificationCost)
                .status(PaymentsModelResponse.StatusEnum.PAID)
                .lastUpdatedDate(new Date());
    }
}
