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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CostUpdateOrchestratorServiceTestIT {
    private CostUpdateOrchestratorService costUpdateOrchestratorService;

    @Mock
    private CostUpdateResultDao costUpdateResultDao;
    @Mock
    private CostComponentsDao costComponentsDao;

    @Mock
    private GpdClient gpdClient;

    private UpdateCostService updateCostService;
    private CostComponentService costComponentService;

    private CostUpdateResultService costUpdateResultService;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
        this.costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        this.updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
        this.costUpdateOrchestratorService = new CostUpdateOrchestratorService(costComponentService, updateCostService);
    }

    // TODO: if this test is exectuted, OnboardInstitutionDaoTestIT.getNewerOfInstant fails!!!!!
    // test handleCostUpdateForIuvs
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
                false
        );
        when(costComponentsDao.updateNotNull(any())).thenReturn(Mono.just(costComponentsEntity));
        when(costComponentsDao.getItem(any(), any())).thenReturn(Mono.just(costComponentsEntity));

        // mock costUpdateResultDao
        when(costUpdateResultDao.insertOrUpdate(any(CostUpdateResultEntity.class)))
                .thenReturn(Mono.just(new CostUpdateResultEntity()));

        // When
        List<UpdateCostResponseInt> result = costUpdateOrchestratorService.handleCostUpdateForIuvs(
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
