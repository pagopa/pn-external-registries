package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.external.registries.config.AccessTokenConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.api.AuthApi;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import it.pagopa.pn.external.registries.exceptions.AssertionGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PDNDClient {

    private AssertionGenerator assertionGenerator;
    private AuthApi authApi;
    private PnExternalRegistriesConfig config;

    public PDNDClient(PnExternalRegistriesConfig config, AssertionGenerator assertionGenerator) {
        this.config = config;
        this.assertionGenerator = assertionGenerator;

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected( connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        ApiClient  newApiClient = new ApiClient(webClient);
        newApiClient.setBasePath(config.getPdndServerUrl());
        authApi = new AuthApi(newApiClient);
    }

    public Mono<ClientCredentialsResponseDto> createToken(String tokenName) throws AssertionGeneratorException {
        AccessTokenConfig accessTokenCfg = config.getAccessTokens().get( tokenName );
        return createToken( accessTokenCfg );
    }

    public Mono<ClientCredentialsResponseDto> createToken( AccessTokenConfig accessTokenCfg ) throws AssertionGeneratorException {

        CompletableFuture<String> futureJwtToken = assertionGenerator.
                                            generateClientAssertion( accessTokenCfg.getJwtCfg() );

        return Mono.fromFuture( futureJwtToken )
                .flatMap( clientAssertion -> {
                            log.debug("createAccessToken clientAssertion={} - clientAssertionType={} - grantType={} - ClientId={} ",
                                    clientAssertion,
                                    accessTokenCfg.getClientAssertionType(),
                                    accessTokenCfg.getGrantType(),
                                    accessTokenCfg.getClientId()
                                );

                            return authApi.createToken(
                                    clientAssertion,
                                    accessTokenCfg.getClientAssertionType(),
                                    accessTokenCfg.getGrantType(),
                                    accessTokenCfg.getClientId()
                                )
                                .map( accessToken -> {
                                    log.debug("createAccessToken accessToken={}", accessToken);
                                    return accessToken;
                                });
                        }
                );
    }

}
