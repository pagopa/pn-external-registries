package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaperCostToInvalidateInt;
import it.pagopa.pn.external.registries.dto.PaymentInfoInt;
import it.pagopa.pn.external.registries.services.PaperCostService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {PaperCostController.class})
class PaperCostControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaperCostService service;

    @Test
    void invalidatePaperCostMapsRequestAndInvokesService() {
        String iun = "testIun";
        String requestBody = """
                {
                  "recIndex": "RECINDEX_0",
                  "costPhases": ["SEND_ANALOG_DOMICILE_ATTEMPT_0", "SEND_ANALOG_DOMICILE_ATTEMPT_1"],
                  "vat": 22,
                  "paymentsInfo": [
                    {
                      "recIndex": 0,
                      "creditorTaxId": "77777777777",
                      "noticeCode": "302000100000019421",
                      "applyCost": true
                    },
                    {
                      "recIndex": 1,
                      "creditorTaxId": "88888888888",
                      "noticeCode": "302000100000019422",
                      "applyCost": false
                    }
                  ]
                }
                """;

        when(service.invalidateCosts(any(PaperCostToInvalidateInt.class), eq(iun))).thenReturn(Mono.empty());

        webTestClient.method(HttpMethod.PUT)
                .uri("/ext-registry-private/cost-update/{iun}/invalidate", iun)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        ArgumentCaptor<PaperCostToInvalidateInt> captor = ArgumentCaptor.forClass(PaperCostToInvalidateInt.class);
        verify(service).invalidateCosts(captor.capture(), eq(iun));

        PaperCostToInvalidateInt capturedRequest = captor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(22, capturedRequest.getVat());
        assertEquals(2, capturedRequest.getCostPhases().size());
        assertEquals(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, capturedRequest.getCostPhases().get(0));
        assertEquals(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_1, capturedRequest.getCostPhases().get(1));
        assertEquals(2, capturedRequest.getPaymentInfoList().size());

        PaymentInfoInt firstPayment = capturedRequest.getPaymentInfoList().get(0);
        assertEquals(0, firstPayment.getRecIndex());
        assertEquals("77777777777", firstPayment.getCreditorTaxId());
        assertEquals("302000100000019421", firstPayment.getNoticeCode());

        PaymentInfoInt secondPayment = capturedRequest.getPaymentInfoList().get(1);
        assertEquals(1, secondPayment.getRecIndex());
        assertEquals("88888888888", secondPayment.getCreditorTaxId());
        assertEquals("302000100000019422", secondPayment.getNoticeCode());
    }
}

