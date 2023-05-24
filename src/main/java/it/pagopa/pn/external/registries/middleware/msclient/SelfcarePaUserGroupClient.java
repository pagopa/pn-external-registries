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
public class SelfcarePaUserGroupClient {

    private final UserGroupApi userGroupPaApi; //inject by name
    private final PnExternalRegistriesConfig config;


    public Mono<PageOfUserGroupResourceDto> getUserGroups(String institutionId) {
        log.logInvokingExternalService("Selfcare PA", "getUserGroups");
        return userGroupPaApi.getUserGroupsUsingGET(config.getSelfcareusergroupUid(), institutionId, 0, 100, null, null, null)
                .doOnNext(pageOfUserGroupResourceDto -> log.info("GetUserGroup result for institutionId {}: {}", institutionId, pageOfUserGroupResourceDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getUserGroups response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Errore lettura usergroups", ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR, x));
                });
    }
}
