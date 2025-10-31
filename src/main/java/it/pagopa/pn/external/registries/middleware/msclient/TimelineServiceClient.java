package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.api.TimelineControllerApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.DeliveryInformationResponse;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.PN_TIMELINE_SERVICE;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_TIMELINE_SERVICE_CLIENT_NOT_FOUND;

@Component
@CustomLog
@RequiredArgsConstructor
public class TimelineServiceClient extends CommonBaseClient {
    private final TimelineControllerApi timelineServiceApi;

    public Mono<DeliveryInformationResponse> getDeliveryInformation(String iun, Integer recIndex) {
        log.logInvokingExternalService(PN_TIMELINE_SERVICE, "TimelineServiceClient");

        return timelineServiceApi.getDeliveryInformation(iun, recIndex)
                .doOnError(throwable -> log.logInvokationResultDownstreamFailed(PN_TIMELINE_SERVICE, "getDeliveryInformation for iun: " + iun + " and recIndex: " + recIndex))
                .onErrorMap(WebClientResponseException.class, error -> {
                    if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        return new PnNotFoundException("Delivery Information not found", "Delivery information with iun " + iun + " and recIndex: " + recIndex + "not found", ERROR_CODE_EXTERNALREGISTRIES_TIMELINE_SERVICE_CLIENT_NOT_FOUND);
                    }
                    return error;
                });
    }
}
