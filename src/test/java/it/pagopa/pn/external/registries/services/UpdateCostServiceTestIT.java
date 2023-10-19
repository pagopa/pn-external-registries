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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class UpdateCostServiceTestIT {

    private UpdateCostService updateCostService;

    @Mock
    private CostUpdateResultDao costUpdateResultDao;

    private CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    @Mock
    private GpdClient gpdClient;

    private final String iun = "iun";
    private final String creditorTaxId = "testTaxId";
    private final String noticeCode = "testNoticeCode";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CostUpdateResultService costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
    }

    @Test
    void testUpdateCost_200() {
        long notificationCost = 100L;

        // Given
        PaymentsModelResponse paymentsModelResponse = new PaymentsModelResponse()
                .iuv(iun)
                .organizationFiscalCode(creditorTaxId)
                .amount(notificationCost)
                .status(PaymentsModelResponse.StatusEnum.PAID)
                .lastUpdatedDate(new Date());

        // GPD client returns a successful response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.ok(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk("testPk");
        entity.setSk("testSk");
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.OK, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }

    @Test
    void testUpdateCost_202() {
        long notificationCost = 100L;

        // Given
        PaymentsModelResponse paymentsModelResponse = new PaymentsModelResponse()
                .iuv(iun)
                .organizationFiscalCode(creditorTaxId)
                .amount(notificationCost)
                .status(PaymentsModelResponse.StatusEnum.PAID)
                .lastUpdatedDate(new Date());

        // GPD client returns a successful response
        ResponseEntity<PaymentsModelResponse> responseEntity = ResponseEntity.status(200).body(paymentsModelResponse);
        when(gpdClient.setNotificationCost(any(), any(), any(), any())).thenReturn(Mono.just(responseEntity));
        System.out.println("responseEntity: " + responseEntity);

        // CostUpdateResultDao returns a successful response
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk("testPk");
        entity.setSk("testSk");
        entity.setEventTimestamp(Instant.now());
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(entity));
        System.out.println("entity: " + entity);

        // When
        UpdateCostResponseInt updateCostResponse = updateCostService.updateCost(
                creditorTaxId, noticeCode, notificationCost, CostUpdateCostPhaseInt.VALIDATION.name(),
                Instant.now(), Instant.now()
        ).block();
        System.out.println("updateCostResponse: " + updateCostResponse);

        // Then
        Assertions.assertNotNull(updateCostResponse, "UpdateCostResponse should not be null");
        Assertions.assertEquals(creditorTaxId, updateCostResponse.getCreditorTaxId(), "CreditorTaxId should match");
        Assertions.assertEquals(noticeCode, updateCostResponse.getNoticeCode(), "NoticeCode should match");
        Assertions.assertEquals(CommunicationResultGroupInt.OK, updateCostResponse.getResult(), "CommunicationResultGroupInt should match");
    }
}
