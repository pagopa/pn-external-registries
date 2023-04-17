package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.api.UserGroupApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR;

@Slf4j
@Component
public class SelfcarePgUserGroupClient extends OcpBaseClient {

    private static final String HEADER_SELFCARE_UID = "x-selfcare-uid";
    private static final int MAX_PAGE_SIZE = 1000;

    private UserGroupApi userGroupApi;
    private final PnExternalRegistriesConfig config;

    public SelfcarePgUserGroupClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), config.getSelfcarepgusergroupApiKey()).build());
        apiClient.setBasePath(config.getSelfcarepgusergroupBaseUrl());
        userGroupApi = new UserGroupApi(apiClient);
    }

    @Override
    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey) {
        return super.initWebClient(builder, apiKey)
                .defaultHeader(HEADER_SELFCARE_UID, config.getSelfcarepgusergroupUid());
    }

    public Mono<PageOfUserGroupResourceDto> getUserGroups(String institutionId) {
        return userGroupApi.getUserGroupsUsingGET(config.getSelfcarepgusergroupUid(), institutionId, 0, MAX_PAGE_SIZE, null, null, null)
                .doOnNext(dto -> log.info("PG GetUserGroup result for institutionId {}: {}", institutionId, dto))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("PG getUserGroups response error {}", e.getResponseBodyAsString(), e);
                    return Mono.error(new PnInternalException("Errore lettura PG usergroups", ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR, e));
                });
    }
}
