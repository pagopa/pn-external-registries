package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class CheckoutClient extends OcpBaseClient {

    private DefaultApi defaultApiClient;
    private final PnExternalRegistriesConfig config;

    public CheckoutClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getCheckoutApiKey()).build());
        apiClient.setBasePath( config.getCheckoutBaseUrl() );
        this.defaultApiClient = new DefaultApi( apiClient );
    }

    public Mono<PaymentRequestsGetResponseDto> getPaymentInfo(String rptIdFromString) throws WebClientResponseException {
        return defaultApiClient.getPaymentInfo( rptIdFromString );
    }
}
