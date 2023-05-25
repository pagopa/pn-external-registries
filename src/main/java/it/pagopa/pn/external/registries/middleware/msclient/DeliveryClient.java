package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.PaymentEventPagoPaPrivate;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@CustomLog
@RequiredArgsConstructor
public class DeliveryClient  {

    private final InternalOnlyApi pnDeliveryApi;


    public Mono<Void> paymentEventPagoPaPrivate(PaymentEventPagoPaPrivate paymentEventPagoPaPrivate) {
        log.logInvokingExternalService("Delivery", "paymentEventPagoPaPrivate");
        return pnDeliveryApi.paymentEventPagoPaPrivate( paymentEventPagoPaPrivate );
    }
}
