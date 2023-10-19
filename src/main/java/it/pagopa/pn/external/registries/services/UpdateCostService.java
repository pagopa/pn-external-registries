package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultRequestInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Mono<UpdateCostResponseInt> updateCost(String creditorTaxId, String noticeCode, Long notificationCost,
                                                  String updateCostPhase, Instant  eventTimestamp, Instant eventStorageTimestamp) {

        String iuv = creditorTaxId + noticeCode;
        String requestId = creditorTaxId + "_" + noticeCode + "_" + updateCostPhase + "_" + UUID.randomUUID();
        Instant communicationTimestamp = Instant.now();

        CostUpdateResultRequestInt request = new CostUpdateResultRequestInt();
        request.setCreditorTaxId(creditorTaxId);
        request.setNoticeCode(noticeCode);
        request.setUpdateCostPhase(CostUpdateCostPhaseInt.valueOf(updateCostPhase));
        request.setRequestId(requestId);
        request.setNotificationCost(notificationCost.intValue());
        request.setIun(iuv);
        request.setEventTimestamp(eventTimestamp);
        request.setEventStorageTimestamp(eventStorageTimestamp);
        request.setCommunicationTimestamp(communicationTimestamp);

        return gpdClient.setNotificationCost(creditorTaxId, noticeCode, requestId, notificationCost)
                .flatMap(response -> {
                    request.setStatusCode(response.getStatusCodeValue());

                    // TODO: move to utility method, with logging
                    PaymentsModelResponse paymentsModelResponse = response.getBody();
                    // remove sensitive information
                    if (paymentsModelResponse != null &&
                            paymentsModelResponse.getTransfer() != null &&
                            !paymentsModelResponse.getTransfer().isEmpty()) {
                        paymentsModelResponse.getTransfer().forEach( obj -> obj.setRemittanceInformation("************"));
                    }
                    // ...
                    request.setJsonResponse(/*response.getBody()*/"");

                    return costUpdateResultService.createUpdateResult(request)
                            .map(result -> new UpdateCostResponseInt(
                                    creditorTaxId,
                                    noticeCode,
                                    result
                            ));
                })
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.info("Error calling GPD: {}, creditorTaxId: {}, noticeCode: {}, requestId: {}, notificationCost: {}",
                            error.getResponseBodyAsString(), creditorTaxId, noticeCode, requestId, notificationCost);

                    request.setStatusCode(error.getRawStatusCode());
                    request.setJsonResponse(error.getResponseBodyAsString());

                    return costUpdateResultService.createUpdateResult(request)
                            .map(result -> new UpdateCostResponseInt(
                                    creditorTaxId,
                                    noticeCode,
                                    result
                            ));
                });
    }
}
