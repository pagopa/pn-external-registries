package it.pagopa.pn.external.registries.pdnd.client;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.api.AuthApi;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PDNDClient {


    private AuthApi authApi;
    private ApiClient newApiClient;

    @Autowired
    PnExternalRegistriesConfig config;

    public PDNDClient() {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        newApiClient = new ApiClient(webClient);

    }

    public Mono<ClientCredentialsResponseDto> createToken() {
        if (newApiClient == null)
            log.info("newApiClient NULL");
        else {
            newApiClient.setBasePath(config.getPdndServerURL());
            authApi = new AuthApi(newApiClient);
        }

        AssertionGenerator assertionGenerator = new AssertionGenerator(config);
        String client_assertion = assertionGenerator.generateClientAssertion();
        log.debug("createToken ... init");

        return authApi.createToken(client_assertion,
                config.getPdndM2MClientAssertionType(),
                config.getPdndM2MGrantType(),
                config.getPdndM2MClientId()
        );
    }

}
