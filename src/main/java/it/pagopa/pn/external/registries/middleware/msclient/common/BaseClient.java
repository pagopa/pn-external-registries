package it.pagopa.pn.external.registries.middleware.msclient.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.ApiClient;
import it.pagopa.pn.external.registries.pdnd.service.AccessTokenCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

public abstract class BaseClient {

    private final AccessTokenCacheService accessTokenCacheService;
    private final String purposeId;
    private ApiClient apiClient;
    private final String basepath;

    protected  BaseClient(AccessTokenCacheService accessTokenCacheService, String purposeId, String basepath){
        this.accessTokenCacheService = accessTokenCacheService;
        this.purposeId = purposeId;
        this.basepath = basepath;
    }

    @PostConstruct
    public void init(){

        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filters(filterList -> {
                    filterList.add(ExchangeFilterFunction.ofRequestProcessor(this::bearerAuthFilter));
                    filterList.add(renewTokenFilter());
                })
                .build();
        apiClient = new ApiClient(webClient);
        apiClient.setBasePath(basepath);
    }


    protected ApiClient getApiClient(){
        return this.apiClient;
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
