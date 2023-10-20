package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultRequestInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class UpdateCostService {
    private final GpdClient gpdClient;
    private final CostUpdateResultService costUpdateResultService;

    @Autowired
    public UpdateCostService(GpdClient gpdClient, CostUpdateResultService costUpdateResultService) {
        this.gpdClient = gpdClient;
        this.costUpdateResultService = costUpdateResultService;
    }

    public Mono<UpdateCostResponseInt> updateCost(String creditorTaxId, String noticeCode, int notificationCost,
                                                  String updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp) {

        String iuv = creditorTaxId + noticeCode;
        String requestId = creditorTaxId + "_" + noticeCode + "_" + updateCostPhase + "_" + UUID.randomUUID();
        Instant communicationTimestamp = Instant.now();

        // log, including passed information and requestId
        log.info("Updating the cost on GPD: iuv: {}, creditorTaxId: {}, noticeCode: {}, requestId: {}, notificationCost: {}",
                iuv, creditorTaxId, noticeCode, requestId, notificationCost);

        return gpdClient.setNotificationCost(creditorTaxId, noticeCode, requestId, (long)notificationCost)
                .flatMap(response -> {

                    PaymentsModelResponse paymentsModelResponse = getPaymentsModelResponseAndCleanUp(response);
                    // convert to JSON
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResponse = null;
                    try {
                        jsonResponse = mapper.writeValueAsString(paymentsModelResponse);
                    } catch (Exception e) {
                        log.error("Error converting paymentsModelResponse to JSON: {}", e.getMessage());
                    }

                    CostUpdateResultRequestInt costUpdateResultRequestInt = getCostUpdateResultRequest(creditorTaxId, noticeCode, notificationCost,
                            updateCostPhase, eventTimestamp, eventStorageTimestamp, communicationTimestamp, requestId, iuv,
                            response.getStatusCodeValue(), jsonResponse);

                    return createUpdateCostResponse(costUpdateResultRequestInt, creditorTaxId, noticeCode);
                })
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.info("Error calling GPD: {}, iuv: {}, creditorTaxId: {}, noticeCode: {}, requestId: {}, notificationCost: {}",
                            error.getResponseBodyAsString(), iuv, creditorTaxId, noticeCode, requestId, notificationCost);

                    CostUpdateResultRequestInt costUpdateResultRequestInt = getCostUpdateResultRequest(creditorTaxId, noticeCode, notificationCost,
                            updateCostPhase, eventTimestamp, eventStorageTimestamp, communicationTimestamp, requestId, iuv,
                            error.getRawStatusCode(), error.getResponseBodyAsString());

                    return createUpdateCostResponse(costUpdateResultRequestInt, creditorTaxId, noticeCode);
                });
    }

    private CostUpdateResultRequestInt getCostUpdateResultRequest(String creditorTaxId, String noticeCode, int notificationCost,
                                                                  String updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp,
                                                                  Instant communicationTimestamp, String requestId, String iuv, int statusCode,
                                                                  String jsonResponse) {
        CostUpdateResultRequestInt costUpdateResultRequestInt = new CostUpdateResultRequestInt();

        costUpdateResultRequestInt.setCreditorTaxId(creditorTaxId);
        costUpdateResultRequestInt.setNoticeCode(noticeCode);
        costUpdateResultRequestInt.setUpdateCostPhase(CostUpdateCostPhaseInt.valueOf(updateCostPhase));
        costUpdateResultRequestInt.setRequestId(requestId);
        costUpdateResultRequestInt.setNotificationCost(notificationCost);
        costUpdateResultRequestInt.setIun(iuv);
        costUpdateResultRequestInt.setEventTimestamp(eventTimestamp);
        costUpdateResultRequestInt.setEventStorageTimestamp(eventStorageTimestamp);
        costUpdateResultRequestInt.setCommunicationTimestamp(communicationTimestamp);
        costUpdateResultRequestInt.setStatusCode(statusCode);
        costUpdateResultRequestInt.setJsonResponse(jsonResponse);

        return costUpdateResultRequestInt;
    }

    private PaymentsModelResponse getPaymentsModelResponseAndCleanUp(ResponseEntity<PaymentsModelResponse> response) {
        PaymentsModelResponse paymentsModelResponse = response.getBody();

        // remove sensitive information
        if (paymentsModelResponse != null &&
                paymentsModelResponse.getTransfer() != null &&
                !paymentsModelResponse.getTransfer().isEmpty()) {
            paymentsModelResponse.getTransfer().forEach( obj -> obj.setRemittanceInformation("************"));
        }

        log.info("Response from GPD after removing sensitive information: {}", paymentsModelResponse);

        return paymentsModelResponse;
    }

    private Mono<UpdateCostResponseInt> createUpdateCostResponse(CostUpdateResultRequestInt request, String creditorTaxId, String noticeCode) {
        return costUpdateResultService.createUpdateResult(request)
                .map(result -> new UpdateCostResponseInt(
                        creditorTaxId,
                        noticeCode,
                        result
                ));
    }
}
