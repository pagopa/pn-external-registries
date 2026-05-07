package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaperCostToInvalidateInt;
import it.pagopa.pn.external.registries.dto.PaymentInfoInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsWithDebtorInfoModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaperCostServiceTest {

    @Mock
    private CostComponentService costComponentService;
    @Mock
    private UpdateCostService updateCostService;
    @Mock
    private GpdClient gpdClient;

    private PaperCostService paperCostService;

    @BeforeEach
    void setUp() {
        paperCostService = new PaperCostService(costComponentService, updateCostService, gpdClient);
    }

    @Test
    void invalidateCostsShouldExecuteWholeFlowWhenPaymentIsUnpaidAndApplyCostTrue() {
        PaperCostToInvalidateInt request = buildRequest(true);
        PaymentsWithDebtorInfoModelResponse gpdResponse = unpaidPaymentResponse();
        UpdateCostResponseInt updateCostResponse = new UpdateCostResponseInt(0, "77777777777", "302000100000019421", CommunicationResultGroupInt.OK);

        when(gpdClient.getOrganizationPaymentOptionByNAV("77777777777", "302000100000019421")).thenReturn(Mono.just(gpdResponse));
        when(costComponentService.getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421")).thenReturn(Mono.just(150));
        when(updateCostService.updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class)))
                .thenReturn(Mono.just(updateCostResponse));
        when(costComponentService.insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, "testIun", 0, "77777777777", "302000100000019421", 0, 22))
                .thenReturn(Mono.just(new CostComponentsInt()));

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .verifyComplete();

        verify(gpdClient, times(1)).getOrganizationPaymentOptionByNAV("77777777777", "302000100000019421");
        verify(costComponentService, times(1)).getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421");
        verify(updateCostService, times(1)).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
        verify(costComponentService, times(1)).insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, "testIun", 0, "77777777777", "302000100000019421", 0, 22);
    }

    @Test
    void invalidateCostsShouldSkipInsertStepCostWhenApplyCostIsFalse() {
        PaperCostToInvalidateInt request = buildRequest(false);
        PaymentsWithDebtorInfoModelResponse gpdResponse = unpaidPaymentResponse();
        UpdateCostResponseInt updateCostResponse = new UpdateCostResponseInt(0, "77777777777", "302000100000019421", CommunicationResultGroupInt.OK);

        when(gpdClient.getOrganizationPaymentOptionByNAV("77777777777", "302000100000019421")).thenReturn(Mono.just(gpdResponse));
        when(costComponentService.getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421")).thenReturn(Mono.just(150));
        when(updateCostService.updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class)))
                .thenReturn(Mono.just(updateCostResponse));

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .verifyComplete();

        verify(gpdClient, times(1)).getOrganizationPaymentOptionByNAV("77777777777", "302000100000019421");
        verify(costComponentService, times(1)).getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421");
        verify(updateCostService, times(1)).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
        verify(costComponentService, never()).insertStepCost(any(), anyString(), anyInt(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void invalidateCostsShouldFailWhenPaymentInfoListIsEmpty() {
        PaperCostToInvalidateInt request = PaperCostToInvalidateInt.builder()
                .vat(22)
                .recIndex("RECINDEX_0")
                .costPhases(List.of(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0))
                .paymentInfoList(List.of())
                .build();

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException
                        && throwable.getMessage().contains("payment information is not available"))
                .verify();

        verify(gpdClient, never()).getOrganizationPaymentOptionByNAV(anyString(), anyString());
        verify(costComponentService, never()).getTotalCost(any(), anyString(), anyInt(), anyString(), anyString());
        verify(updateCostService, never()).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
    }

    @Test
    void invalidateCostsShouldFailWhenPaymentStatusIsClosed() {
        PaperCostToInvalidateInt request = buildRequest(true);
        PaymentsWithDebtorInfoModelResponse gpdResponse = new PaymentsWithDebtorInfoModelResponse();
        gpdResponse.setStatus(PaymentsWithDebtorInfoModelResponse.StatusEnum.PO_PAID);

        when(gpdClient.getOrganizationPaymentOptionByNAV("77777777777", "302000100000019421")).thenReturn(Mono.just(gpdResponse));

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .expectErrorMatches(throwable -> throwable instanceof PnInternalException
                        && throwable.getMessage().contains("Pagato"))
                .verify();

        verify(costComponentService, never()).getTotalCost(any(), anyString(), anyInt(), anyString(), anyString());
        verify(updateCostService, never()).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
        verify(costComponentService, never()).insertStepCost(any(), anyString(), anyInt(), anyString(), anyString(), anyInt(), any());
    }

    private PaperCostToInvalidateInt buildRequest(boolean applyCost) {
        PaymentInfoInt paymentInfo = PaymentInfoInt.builder()
                .recIndex(0)
                .creditorTaxId("77777777777")
                .noticeCode("302000100000019421")
                .applyCost(applyCost)
                .build();

        return PaperCostToInvalidateInt.builder()
                .recIndex("RECINDEX_0")
                .vat(22)
                .costPhases(List.of(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0))
                .paymentInfoList(List.of(paymentInfo))
                .build();
    }

    private PaymentsWithDebtorInfoModelResponse unpaidPaymentResponse() {
        PaymentsWithDebtorInfoModelResponse response = new PaymentsWithDebtorInfoModelResponse();
        response.setStatus(PaymentsWithDebtorInfoModelResponse.StatusEnum.PO_UNPAID);
        return response;
    }
}
