package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {UpdateNotificationCostController.class})

class UpdateNotificationCostControllerTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CostUpdateOrchestratorService service;

    @Test
    void updateNotificationCost() throws JsonProcessingException {
        //Given
        String url = "/ext-registry-private/cost-update";

        UpdateNotificationCostRequestDto request = new UpdateNotificationCostRequestDto();
        request.setIun("testIun");
        request.setNotificationStepCost(100);
        request.setUpdateCostPhase(UpdateNotificationCostRequestDto.UpdateCostPhaseEnum.NOTIFICATION_CANCELLED);
        request.setEventTimestamp(new Date());
        request.setEventStorageTimestamp(new Date());

        List<PaymentsInfoForRecipientDto> list = new ArrayList<>();
        PaymentsInfoForRecipientDto dto1 = new PaymentsInfoForRecipientDto();
        dto1.setCreditorTaxId("77777777777");
        dto1.setRecIndex(0);
        dto1.setNoticeCode("222222222222222222");
        
        PaymentsInfoForRecipientDto dto2 = new PaymentsInfoForRecipientDto();
        dto2.setCreditorTaxId("77777777777");
        dto2.setRecIndex(0);
        dto2.setNoticeCode("222222222222222223");
        list.add(dto1);
        list.add(dto2);
        
        request.setPaymentsInfoForRecipients(list);

        final List<UpdateCostResponseInt> listUpdateCostResponse = getUpdateCostResponseInts(dto1, dto2);
    
        when(service.handleCostUpdateForIuvs(
                        Mockito.isNull(),
                        Mockito.eq(request.getNotificationStepCost()),
                        Mockito.eq(request.getIun()),
                        Mockito.any(),
                        Mockito.eq(request.getEventTimestamp().toInstant()),
                        Mockito.eq(request.getEventStorageTimestamp().toInstant()),
                        Mockito.any()))
                .thenReturn(Flux.fromIterable(listUpdateCostResponse));
        
        // Then
        final UpdateNotificationCostResponseDto expectedResponse = getExpectedResponse(listUpdateCostResponse);

        webTestClient.method(HttpMethod.POST)
                .uri(url)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(expectedResponse));
    }

    @NotNull
    private static UpdateNotificationCostResponseDto getExpectedResponse(List<UpdateCostResponseInt> listUpdateCostResponse) {
        UpdateNotificationCostResultDto expectedResponse1 = new UpdateNotificationCostResultDto();
        expectedResponse1.setNoticeCode(listUpdateCostResponse.get(0).getNoticeCode());
        expectedResponse1.setRecIndex(listUpdateCostResponse.get(0).getRecIndex());
        expectedResponse1.setCreditorTaxId(listUpdateCostResponse.get(0).getCreditorTaxId());
        expectedResponse1.setResult(UpdateNotificationCostResultDto.ResultEnum.valueOf(listUpdateCostResponse.get(0).getResult().getValue()));

        UpdateNotificationCostResultDto expectedResponse2 = new UpdateNotificationCostResultDto();
        expectedResponse2.setNoticeCode(listUpdateCostResponse.get(1).getNoticeCode());
        expectedResponse2.setRecIndex(listUpdateCostResponse.get(1).getRecIndex());
        expectedResponse2.setCreditorTaxId(listUpdateCostResponse.get(1).getCreditorTaxId());
        expectedResponse2.setResult(UpdateNotificationCostResultDto.ResultEnum.valueOf(listUpdateCostResponse.get(1).getResult().getValue()));

        List<UpdateNotificationCostResultDto> expectedListResponse = new ArrayList<>();
        expectedListResponse.add(expectedResponse1);
        expectedListResponse.add(expectedResponse2);

        UpdateNotificationCostResponseDto expectedResponse = new UpdateNotificationCostResponseDto();
        expectedResponse.setUpdateResults(expectedListResponse);
        return expectedResponse;
    }

    @NotNull
    private static List<UpdateCostResponseInt> getUpdateCostResponseInts(PaymentsInfoForRecipientDto dto1, PaymentsInfoForRecipientDto dto2) {
        List<UpdateCostResponseInt> listUpdateCostResponse = new ArrayList<>();
        UpdateCostResponseInt response1 = new UpdateCostResponseInt();
        response1.setCreditorTaxId(dto1.getCreditorTaxId());
        response1.setRecIndex(dto1.getRecIndex());
        response1.setNoticeCode(dto1.getNoticeCode());
        response1.setResult(CommunicationResultGroupInt.KO);
        
        UpdateCostResponseInt response2 = new UpdateCostResponseInt();
        response2.setCreditorTaxId(dto2.getCreditorTaxId());
        response2.setRecIndex(dto2.getRecIndex());
        response2.setNoticeCode(dto2.getNoticeCode());
        response2.setResult(CommunicationResultGroupInt.OK);

        listUpdateCostResponse.add(response1);
        listUpdateCostResponse.add(response2);
        return listUpdateCostResponse;
    }

}
