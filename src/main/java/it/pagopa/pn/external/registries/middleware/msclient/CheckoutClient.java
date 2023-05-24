package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.CartRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
@CustomLog
public class CheckoutClient extends OcpBaseClient {

    private DefaultApi defaultApiClient;
    private DefaultApi defaultApiClientCartCheckout; //checkout ha una base-url diversa per il carrello
    private final PnExternalRegistriesConfig config;

    public CheckoutClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getCheckoutApiKey()).build());
        apiClient.setBasePath( config.getCheckoutApiBaseUrl() );
        this.defaultApiClient = new DefaultApi( apiClient );

        ApiClient apiClientCartCheckout = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        apiClientCartCheckout.setBasePath(config.getCheckoutCartApiBaseUrl());
        this.defaultApiClientCartCheckout = new DefaultApi(apiClientCartCheckout);
    }

    public Mono<PaymentRequestsGetResponseDto> getPaymentInfo(String rptIdFromString) throws WebClientResponseException {
        log.logInvokingExternalService("Checkout", "getPaymentInfo");
        return defaultApiClient.getPaymentInfo( rptIdFromString );
    }

    public Mono<ResponseEntity<Void>> checkoutCart(CartRequestDto cartRequestDto) throws WebClientResponseException {
        log.logInvokingExternalService("Checkout", "checkoutCart");
        return defaultApiClientCartCheckout.postCartsWithHttpInfo( cartRequestDto );
    }

}
