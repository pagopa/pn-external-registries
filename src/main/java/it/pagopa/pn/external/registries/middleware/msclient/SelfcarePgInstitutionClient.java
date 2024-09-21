package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.SELFCARE_PA;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class SelfcarePgInstitutionClient {

    private final InstitutionsApi institutionsPgApi;

    public Mono<UserInstitutionResourceDto> retrieveUserInstitution(String userIdForAuth, String institutionId){
        log.info("Retrieving user institutions for userId {}", userIdForAuth);
        return institutionsPgApi.getUserInstitutionsUsingGET(institutionId, userIdForAuth, null, UserProductResourceDto.StatusEnum.ACTIVE.name() , null, null, null,null)
                .doOnNext(institutionsResponseDto -> log.info("getInstitutions result: {}", institutionsResponseDto))
                .collectList()
                .filter(userInstitutionResourceDtos -> !userInstitutionResourceDtos.isEmpty())
                .switchIfEmpty(Mono.error(new PnInternalException("Error getting institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR)))
                .map(userInstitutionResourceDtos -> userInstitutionResourceDtos.get(0))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.logInvokationResultDownstreamFailed(SELFCARE_PA, CommonBaseClient.elabExceptionMessage(x));
                    log.error("getInstitutions for userId " + userIdForAuth + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }


}
