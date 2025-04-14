package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.api.PaymentRequestsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.CartRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.CHECKOUT;

@Component
@CustomLog
public class CheckoutClient extends OcpBaseClient {

    private final PaymentRequestsApi paymentRequestsApi;
    private final DefaultApi defaultApiClientCartCheckout; //checkout ha una base-url diversa per il carrello

    //inject by name
    public CheckoutClient(PaymentRequestsApi paymentRequestsApi, DefaultApi defaultApiClientCartCheckout) {
        this.paymentRequestsApi = paymentRequestsApi;
        this.defaultApiClientCartCheckout = defaultApiClientCartCheckout;
    }

    public Mono<PaymentRequestsGetResponseDto> getPaymentInfo(String rptId) throws WebClientResponseException {
        log.logInvokingExternalDownstreamService(CHECKOUT, "getPaymentInfo");
        return paymentRequestsApi.getPaymentRequestInfo( rptId )
                .doOnError(throwable -> log.logInvokationResultDownstreamFailed(CHECKOUT, elabExceptionMessage(throwable)));
    }

    public Mono<ResponseEntity<Void>> checkoutCart(CartRequestDto cartRequestDto) throws WebClientResponseException {
        log.logInvokingExternalDownstreamService(CHECKOUT, "checkoutCart");
        return defaultApiClientCartCheckout.postCartsWithHttpInfo( cartRequestDto )
                .doOnError(throwable -> log.logInvokationResultDownstreamFailed(CHECKOUT, elabExceptionMessage(throwable)));
    }

}
