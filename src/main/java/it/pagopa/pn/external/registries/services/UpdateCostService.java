package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultRequestInt;
import it.pagopa.pn.external.registries.dto.UpdateCostOptionEnum;
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

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_INVALIDATE_COST_FAILED;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PAYMENT_ONGOING;

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

    public Mono<UpdateCostResponseInt> updateCost(int recIndex, String iun, String creditorTaxId, String noticeCode, int notificationCost,
                                                  CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp) {

        return updateCost(recIndex, iun, creditorTaxId, noticeCode, notificationCost, updateCostPhase,
                eventTimestamp, eventStorageTimestamp, UpdateCostOptionEnum.NO_CHECK_RESPONSE_STATUSES, false);
    }

    public Mono<UpdateCostResponseInt> updateCostForInvalidated(int recIndex, String iun, String creditorTaxId, String noticeCode, int notificationCost,
                                                                CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp) {

        return updateCost(recIndex, iun, creditorTaxId, noticeCode, notificationCost, updateCostPhase,
                eventTimestamp, eventStorageTimestamp, UpdateCostOptionEnum.CHECK_RESPONSE_STATUSES, true);
    }

    private Mono<UpdateCostResponseInt> updateCost(int recIndex, String iun, String creditorTaxId, String noticeCode, int notificationCost,
                                                   CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp,
                                                   UpdateCostOptionEnum updateCostOption, boolean reworked) {

        String iuv = creditorTaxId + noticeCode;
        String requestId = creditorTaxId + "_" + noticeCode + "_" + updateCostPhase + "_" + UUID.randomUUID();
        Instant communicationTimestamp = Instant.now();

        // log, including passed information and requestId
        log.info("Updating the cost on GPD: iuv: {}, creditorTaxId: {}, noticeCode: {}, requestId: {}, notificationCost: {}",
                iuv, creditorTaxId, noticeCode, requestId, notificationCost);

        Mono<ResponseEntity<PaymentsModelResponse>> setNotificationCostResponse = gpdClient.setNotificationCost(creditorTaxId, noticeCode, requestId, (long) notificationCost);
        if (updateCostOption.isCheckResponseStatuses()) {
            setNotificationCostResponse = setNotificationCostResponse.flatMap(UpdateCostService::checkForResponseStatuses);
        }

        return setNotificationCostResponse
                .flatMap(response -> processNotificationCostResponse(recIndex, iun, creditorTaxId, noticeCode, notificationCost, updateCostPhase, eventTimestamp, eventStorageTimestamp, response, communicationTimestamp, requestId, reworked))
                .onErrorResume(WebClientResponseException.class, error -> processNotificationCostResponseError(recIndex, iun, creditorTaxId, noticeCode, notificationCost, updateCostPhase, eventTimestamp, eventStorageTimestamp, error, iuv, requestId, communicationTimestamp, reworked));
    }

    private Mono<UpdateCostResponseInt> processNotificationCostResponseError(int recIndex, String iun, String creditorTaxId, String noticeCode, int notificationCost, CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp, WebClientResponseException error, String iuv, String requestId, Instant communicationTimestamp, boolean reworked) {
        log.info("Error calling GPD: {}, iuv: {}, creditorTaxId: {}, noticeCode: {}, requestId: {}, notificationCost: {}",
                error.getResponseBodyAsString(), iuv, creditorTaxId, noticeCode, requestId, notificationCost);

        CostUpdateResultRequestInt costUpdateResultRequestInt = getCostUpdateResultRequest(creditorTaxId, noticeCode, notificationCost,
                updateCostPhase, eventTimestamp, eventStorageTimestamp, communicationTimestamp, requestId, iun,
                error.getStatusCode().value(), error.getResponseBodyAsString());

        return createUpdateCostResponse(costUpdateResultRequestInt, recIndex, creditorTaxId, noticeCode, reworked);
    }

    private Mono<UpdateCostResponseInt> processNotificationCostResponse(int recIndex, String iun, String creditorTaxId, String noticeCode, int notificationCost, CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp, ResponseEntity<PaymentsModelResponse> response, Instant communicationTimestamp, String requestId, boolean reworked) {
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
                updateCostPhase, eventTimestamp, eventStorageTimestamp, communicationTimestamp, requestId, iun,
                response.getStatusCode().value(), jsonResponse);

        return createUpdateCostResponse(costUpdateResultRequestInt, recIndex, creditorTaxId, noticeCode,reworked);
    }

    private static Mono<ResponseEntity<PaymentsModelResponse>> checkForResponseStatuses(ResponseEntity<PaymentsModelResponse> response) {
        return switch (response.getStatusCode().value()) {
            case 200 -> Mono.just(response);
            case 209 -> {
                log.warn("GPD returned status 209 for cost update, so status is OK_IN_PAYMENT");
                yield Mono.just(response);
            }
            case 422 ->
                    Mono.error(new PnRuntimeException(
                            "Posizione debitoria considerata chiusa.",
                            "Posizione debitoria considerata chiusa.",
                            response.getStatusCode().value(),
                            ERROR_CODE_EXTERNALREGISTRIES_PAYMENT_ONGOING,
                            null,
                            null
                    ));
            default ->
                    Mono.error(new PnRuntimeException(
                            "Updating the cost for invalidated elements returned error.",
                            "Updating the cost for invalidated elements returned error.",
                            response.getStatusCode().value(),
                            ERROR_CODE_EXTERNALREGISTRIES_INVALIDATE_COST_FAILED,
                            null,
                            null
                    ));
        };
    }

    private CostUpdateResultRequestInt getCostUpdateResultRequest(String creditorTaxId, String noticeCode, int notificationCost,
                                                                  CostUpdateCostPhaseInt updateCostPhase, Instant eventTimestamp, Instant eventStorageTimestamp,
                                                                  Instant communicationTimestamp, String requestId, String iun, int statusCode,
                                                                  String jsonResponse) {
        CostUpdateResultRequestInt costUpdateResultRequestInt = new CostUpdateResultRequestInt();

        costUpdateResultRequestInt.setCreditorTaxId(creditorTaxId);
        costUpdateResultRequestInt.setNoticeCode(noticeCode);
        costUpdateResultRequestInt.setUpdateCostPhase(updateCostPhase);
        costUpdateResultRequestInt.setRequestId(requestId);
        costUpdateResultRequestInt.setNotificationCost(notificationCost);
        costUpdateResultRequestInt.setIun(iun);
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

    private Mono<UpdateCostResponseInt> createUpdateCostResponse(CostUpdateResultRequestInt request, int recIndex, String creditorTaxId, String noticeCode, boolean reworked) {
        return costUpdateResultService.createUpdateResult(request, reworked)
                .map(result -> new UpdateCostResponseInt(
                        recIndex,
                        creditorTaxId,
                        noticeCode,
                        result
                ));
    }
}
