package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.*;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        paperCostService = new PaperCostService(costComponentService, updateCostService);
    }

    @Test
    void invalidateCostsShouldExecuteWholeFlowWhenPaymentIsUnpaidAndApplyCostTrue() {
        PaperCostToInvalidateInt request = buildRequest();
        UpdateCostResponseInt updateCostResponse = new UpdateCostResponseInt(0, "77777777777", "302000100000019421", CommunicationResultGroupInt.OK);

        when(costComponentService.getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421")).thenReturn(Mono.just(150));
        when(updateCostService.updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class)))
                .thenReturn(Mono.just(updateCostResponse));
        when(costComponentService.insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, "testIun", 0, "77777777777", "302000100000019421", 0, 22))
                .thenReturn(Mono.just(new CostComponentsInt()));

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .verifyComplete();

        verify(costComponentService, times(1)).getTotalCost(22, "testIun", 0, "77777777777", "302000100000019421");
        verify(updateCostService, times(1)).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
        verify(costComponentService, times(1)).insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, "testIun", 0, "77777777777", "302000100000019421", 0, 22);
    }

    @Test
    void invalidateCostsShouldFailWhenPaymentInfoListIsEmpty() {
        PaperCostToInvalidateInt request = PaperCostToInvalidateInt.builder()
                .vat(22)
                .costPhases(List.of(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0))
                .paymentInfoList(List.of())
                .build();

        StepVerifier.create(paperCostService.invalidateCosts(request, "testIun"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("payment information is not available"))
                .verify();

        verify(gpdClient, never()).getOrganizationPaymentOptionByNAV(anyString(), anyString());
        verify(costComponentService, never()).getTotalCost(any(), anyString(), anyInt(), anyString(), anyString());
        verify(updateCostService, never()).updateCostForInvalidated(anyInt(), anyString(), anyString(), anyString(), anyInt(), any(), any(Instant.class), any(Instant.class));
    }

    private PaperCostToInvalidateInt buildRequest() {
        PaymentInfoInt paymentInfo = PaymentInfoInt.builder()
                .recIndex(0)
                .creditorTaxId("77777777777")
                .noticeCode("302000100000019421")
                .build();

        return PaperCostToInvalidateInt.builder()
                .vat(22)
                .costPhases(List.of(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0))
                .paymentInfoList(List.of(paymentInfo))
                .build();
    }

}
