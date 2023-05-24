package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.dto.ProbableSchedulingAnalogDateResponse;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@CustomLog
@RequiredArgsConstructor
public class DeliveryPushClient extends CommonBaseClient {

    private final TimelineAndStatusApi pnDeliveryPushApi;

    public Mono<ResponseEntity<ProbableSchedulingAnalogDateResponse>> getSchedulingAnalogDateWithHttpInfo(String iun, String recipientId) {
        log.logInvokingExternalService("Delivery Push", "getSchedulingAnalogDate");
        return pnDeliveryPushApi.getSchedulingAnalogDateWithHttpInfo(iun, recipientId);
    }
}
