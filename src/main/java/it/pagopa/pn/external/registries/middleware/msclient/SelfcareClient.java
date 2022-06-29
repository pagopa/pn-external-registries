package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.api.UserGroupsApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.UserGroupPlainResourceDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class SelfcareClient extends OcpBaseClient {

    private static final String HEADER_SELFCARE_UID = "x-selfcare-uid";

    private InstitutionsApi institutionsApi;
    private UserGroupsApi userGroupsApi;
    private final PnExternalRegistriesConfig config;

    public SelfcareClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {

        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), config.getSelfcareApiKey()).build());
        apiClient.setBasePath(config.getSelfcareBaseUrl());
        this.institutionsApi = new InstitutionsApi( apiClient );

        apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), config.getSelfcareApiKey()).build());
        apiClient.setBasePath(config.getSelfcareBaseUrl());
        this.userGroupsApi = new UserGroupsApi( apiClient );
    }

    @Override
    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){
        return super.initWebClient(builder, apiKey)
                .defaultHeader(HEADER_SELFCARE_UID,config.getSelfcareUid());
    }

    public Mono<InstitutionResourceDto> getInstitution(String institutionId) throws WebClientResponseException {

        return institutionsApi.getInstitutionUsingGET(institutionId)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getInstitution response error {}", x.getResponseBodyAsString(), x);
                    if (x.getStatusCode() == HttpStatus.NOT_FOUND)
                        return Mono.error(new NotFoundException());

                    return Mono.error(new InternalErrorException());
                });
    }


    public Flux<InstitutionResourceDto> getInstitutions() throws WebClientResponseException {

        return institutionsApi.getInstitutionsUsingGET()
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getInstitutions response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new InternalErrorException());
                });
    }

    public Flux<UserGroupPlainResourceDto> getUserGroups(String institutionId) {
        return userGroupsApi.getUserGroupsUsingGET(institutionId, 0, 100, null, config.getSelfcarePnProductId(), null)
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