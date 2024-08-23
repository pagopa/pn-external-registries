package it.pagopa.pn.external.registries.middleware.msclient;

import com.amazonaws.util.StringUtils;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.UserGroupApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.PageOfUserGroupResourceDto;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.SELFCARE_PG;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class SelfcarePgUserGroupClient {

    private static final int MAX_PAGE_SIZE = 1000;

    private final UserGroupApi userGroupPgApi; //inject by name
    private final PnExternalRegistriesConfig config;


    public Mono<PageOfUserGroupResourceDto> getUserGroups(String institutionId, String userId) {
        log.logInvokingExternalDownstreamService(SELFCARE_PG, "getUserGroups");
        UUID userIdUuid = StringUtils.isNullOrEmpty(userId) ? null: UUID.fromString(userId);
        return userGroupPgApi.getUserGroupsUsingGET(config.getSelfcarepgusergroupUid(), institutionId, 0, MAX_PAGE_SIZE, null, userIdUuid, null)
                .doOnNext(dto -> log.info("PG GetUserGroup result for institutionId {}: {}", institutionId, dto))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.logInvokationResultDownstreamFailed(SELFCARE_PG, CommonBaseClient.elabExceptionMessage(e));
                    log.error("PG getUserGroups response error {}", e.getResponseBodyAsString(), e);
                    return Mono.error(new PnInternalException("Errore lettura PG usergroups", ERROR_CODE_EXTERNALREGISTRIES_USERGROUPSREADERROR, e));
                });
    }
}
