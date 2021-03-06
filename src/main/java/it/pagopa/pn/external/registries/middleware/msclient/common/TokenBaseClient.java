package it.pagopa.pn.external.registries.middleware.msclient.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.services.AccessTokenCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

public abstract class TokenBaseClient extends CommonBaseClient {

    private final AccessTokenCacheService accessTokenCacheService;
    private final String purposeId;

    protected TokenBaseClient(AccessTokenCacheService accessTokenCacheService, String purposeId){
        this.accessTokenCacheService = accessTokenCacheService;
        this.purposeId = purposeId;
    }

    protected WebClient initWebClient(WebClient.Builder builder){

        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        return super.enrichBuilder(builder)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filters(filterList -> {
                    filterList.add(ExchangeFilterFunction.ofRequestProcessor(this::bearerAuthFilter));
                    filterList.add(renewTokenFilter());
                })
                .build();
    }


    private Mono<ClientRequest> bearerAuthFilter(ClientRequest request) {
        return accessTokenCacheService.getToken(this.purposeId,false)
                .map(token -> ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build());
    }

    private ExchangeFilterFunction renewTokenFilter() {
        return (request, next) -> next.exchange(request).flatMap(response -> {
            if (response.statusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                return response.releaseBody()
                        .then(accessTokenCacheService.getToken(this.purposeId, true))
                        .flatMap(token -> {
                            ClientRequest newRequest = ClientRequest.from(request)
                                    .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                    .build();
                            return next.exchange(newRequest);
                        });
            } else {
                return Mono.just(response);
            }
        });
    }

}
