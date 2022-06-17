package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class IOClient {

    private static final String HEADER_API_KEY = "Ocp-Apim-Subscription-Key";

    private DefaultApi defaultApiClient;
    private final PnExternalRegistriesConfig config;

    public IOClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {

        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HEADER_API_KEY, config.getIoApiKey())
                .build();

        ApiClient apiClient = new ApiClient( webClient );
        apiClient.setBasePath( config.getIoBaseUrl() );
        this.defaultApiClient = new DefaultApi( apiClient );
    }

    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        return defaultApiClient.submitMessageforUserWithFiscalCodeInBody( message );
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        return defaultApiClient.getProfileByPOST( payload );
    }
}
