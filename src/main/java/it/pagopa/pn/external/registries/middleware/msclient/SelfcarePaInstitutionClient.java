package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.SELFCARE_PA;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class SelfcarePaInstitutionClient {

    private final InstitutionsApi institutionsApi;
    private final PnExternalRegistriesConfig config;

    public Flux<InstitutionResourceDto> getInstitutions(String userIdForAuth) {
        log.logInvokingExternalService(SELFCARE_PA, "getInstitutions");
        return institutionsApi.getInstitutionsUsingGET(userIdForAuth)
                .doOnNext(institutionsResponseDto -> log.info("getInstitutions result: {}", institutionsResponseDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getInstitutions for userId " + userIdForAuth + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }

    public Flux<ProductResourceDto> getInstitutionProducts(String institutionId, String userId) {
        log.logInvokingExternalService(SELFCARE_PA, "getInstitutions");
        return institutionsApi.getInstitutionUserProductsUsingGET(institutionId, userId)
                .doOnNext(productResourceDto -> log.info("getInstitutionProduct result: {}", productResourceDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getInstitutionProduct for institutionId " + institutionId + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting product institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }


}
