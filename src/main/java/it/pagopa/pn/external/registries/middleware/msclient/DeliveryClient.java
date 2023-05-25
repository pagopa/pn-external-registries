package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.PaymentEventPagoPaPrivate;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.PN_DELIVERY;

@Component
@CustomLog
@RequiredArgsConstructor
public class DeliveryClient  {

    private final InternalOnlyApi pnDeliveryApi;


    public Mono<Void> paymentEventPagoPaPrivate(PaymentEventPagoPaPrivate paymentEventPagoPaPrivate) {
        log.logInvokingExternalService(PN_DELIVERY, "paymentEventPagoPaPrivate");
        return pnDeliveryApi.paymentEventPagoPaPrivate( paymentEventPagoPaPrivate );
    }
}
