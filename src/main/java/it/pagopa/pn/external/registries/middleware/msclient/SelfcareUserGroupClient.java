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
        return userGroupsApi.getUserGroupsUsingGET(config.getSelfcareusergroupUid(), institutionId, 0, 100, null, null, null)
                .doOnNext(pageOfUserGroupResourceDto -> log.info("GetUserGroup result for institutionId {}: {}", institutionId, pageOfUserGroupResourceDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getUserGroups response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Errore lettura usergroups", ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR, x));
                });
    }
}
