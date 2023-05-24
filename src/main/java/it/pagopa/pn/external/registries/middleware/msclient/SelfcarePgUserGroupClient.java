package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.api.UserGroupApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.PageOfUserGroupResourceDto;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class SelfcarePgUserGroupClient {

    private static final int MAX_PAGE_SIZE = 1000;

    private final UserGroupApi userGroupPgApi; //inject by name
    private final PnExternalRegistriesConfig config;


    public Mono<PageOfUserGroupResourceDto> getUserGroups(String institutionId) {
        log.logInvokingExternalService("Selfcare PG", "getUserGroups");
        return userGroupPgApi.getUserGroupsUsingGET(config.getSelfcarepgusergroupUid(), institutionId, 0, MAX_PAGE_SIZE, null, null, null)
                .doOnNext(dto -> log.info("PG GetUserGroup result for institutionId {}: {}", institutionId, dto))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("PG getUserGroups response error {}", e.getResponseBodyAsString(), e);
                    return Mono.error(new PnInternalException("Errore lettura PG usergroups", ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR, e));
                });
    }
}
