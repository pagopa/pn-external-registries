package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.SentNotificationV25;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery_reactive.v1.api.InternalOnlyApi;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.PN_DELIVERY;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_DELIVERY_CLIENT_NOT_FOUND;

@Component
@CustomLog
@RequiredArgsConstructor
public class DeliveryClient extends CommonBaseClient {
    private final InternalOnlyApi pnDeliveryApi;

    public Mono<SentNotificationV25> getSentNotificationPrivate(String iun) {
        log.logInvokingExternalService(PN_DELIVERY, "getSentNotification");

        return pnDeliveryApi.getSentNotificationPrivate(iun)
                .doOnError(throwable -> {
                    log.logInvokationResultDownstreamFailed(PN_DELIVERY, String.format("Sent notification not found for iun: %s. Exception: %s - %s", iun, throwable.getClass().getSimpleName(), throwable.getMessage()));
                })
                .onErrorMap(WebClientResponseException.class, error -> {
                    if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        return new PnNotFoundException("Sent notification not found.", "Sent notification with iun " + iun + " does not exist", ERROR_CODE_EXTERNALREGISTRIES_DELIVERY_CLIENT_NOT_FOUND);
                    }
                return error;
                });
    }

}
