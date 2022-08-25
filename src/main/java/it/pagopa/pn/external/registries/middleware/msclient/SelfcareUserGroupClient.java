package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.api.UserGroupApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;

@Component
@Slf4j
public class SelfcareUserGroupClient extends OcpBaseClient {

    private static final String HEADER_SELFCARE_UID = "x-selfcare-uid";

    private UserGroupApi userGroupsApi;
    private final PnExternalRegistriesConfig config;

    public SelfcareUserGroupClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), config.getSelfcareusergroupApiKey()).build());
        apiClient.setBasePath(config.getSelfcareusergroupBaseUrl());
        this.userGroupsApi = new UserGroupApi( apiClient );
    }

    @Override
    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){
        return super.initWebClient(builder, apiKey)
                .defaultHeader(HEADER_SELFCARE_UID,config.getSelfcareusergroupUid());
    }

    public Mono<PageOfUserGroupResourceDto> getUserGroups(String institutionId) {
        return userGroupsApi.getUserGroupsUsingGET(config.getSelfcareusergroupUid(), institutionId, 0, 100, null, config.getSelfcareusergroupPnProductId(), null, null)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getUserGroups response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new InternalErrorException());
                });
    }
}
