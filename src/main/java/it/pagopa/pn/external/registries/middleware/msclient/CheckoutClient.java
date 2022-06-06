package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class CheckoutClient {
    private static final String HEADER_API_KEY = "Ocp-Apim-Subscription-Key";

    private DefaultApi defaultApiClient;
    private final PnExternalRegistriesConfig config;

    public CheckoutClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {

        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HEADER_API_KEY, config.getCheckoutApiKey())
                .build();

        ApiClient apiClient = new ApiClient( webClient );
        apiClient.setBasePath( config.getCheckoutBaseUrl() );
        this.defaultApiClient = new DefaultApi( apiClient );
    }

    public Mono<PaymentRequestsGetResponseDto> getPaymentInfo(String rptIdFromString) throws WebClientResponseException {
        return defaultApiClient.getPaymentInfo( rptIdFromString );
    }
}
