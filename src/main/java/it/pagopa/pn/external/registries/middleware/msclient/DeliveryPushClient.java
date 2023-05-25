package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.deliverypush.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.deliverypush.v1.dto.ProbableSchedulingAnalogDateResponse;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.PN_DELIVERY_PUSH;

@Component
@CustomLog
@RequiredArgsConstructor
public class DeliveryPushClient extends CommonBaseClient {

    private final TimelineAndStatusApi pnDeliveryPushApi;

    public Mono<ResponseEntity<ProbableSchedulingAnalogDateResponse>> getSchedulingAnalogDateWithHttpInfo(String iun, String recipientId) {
        log.logInvokingExternalService(PN_DELIVERY_PUSH, "getSchedulingAnalogDate");
        return pnDeliveryPushApi.getSchedulingAnalogDateWithHttpInfo(iun, recipientId);
    }
}
