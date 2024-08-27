package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.SELFCARE_PA;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR;

@CustomLog
@Component
@RequiredArgsConstructor
public class SelfcarePaInstitutionClient {

    private final InstitutionsApi institutionsApi;
    private final PnExternalRegistriesConfig config;

    public Flux<InstitutionResourceDto> getInstitutions(String userIdForAuth) {
        log.logInvokingExternalDownstreamService(SELFCARE_PA, "getInstitutions");
        return institutionsApi.getInstitutionsUsingGET(userIdForAuth)
                .doOnNext(institutionsResponseDto -> log.info("getInstitutions result: {}", institutionsResponseDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.logInvokationResultDownstreamFailed(SELFCARE_PA, CommonBaseClient.elabExceptionMessage(x));
                    log.error("getInstitutions for userId " + userIdForAuth + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }

    public Flux<UserInstitutionResourceDto> getUserInstitutions(String userIdForAuth){
        int size = 100;
        log.logInvokingExternalDownstreamService(SELFCARE_PA, "getUserInstitutions");
        AtomicInteger pageCounter = new AtomicInteger(0);
        return Flux.defer(() -> {
                    int currentPage = pageCounter.getAndIncrement();
                    return retrievePaginatedUserInstitutions(userIdForAuth, currentPage, size);
                })
                .repeat().takeUntil(institutionsResponseDto -> CollectionUtils.isEmpty(institutionsResponseDto) || institutionsResponseDto.size() < size)
                .flatMapIterable(userInstitutionResourceDtos -> userInstitutionResourceDtos);
    }

    public Mono<List<UserInstitutionResourceDto>> retrievePaginatedUserInstitutions(String userIdForAuth, Integer page, Integer size){
        log.info("Retrieving user institutions for userId {} page {}", userIdForAuth, page);
        return institutionsApi.getUserInstitutionsUsingGET(null, userIdForAuth, null, null, null, null, page,size)
                .doOnNext(institutionsResponseDto -> log.info("getInstitutions result: {}", institutionsResponseDto))
                .collectList()
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.logInvokationResultDownstreamFailed(SELFCARE_PA, CommonBaseClient.elabExceptionMessage(x));
                    log.error("getInstitutions for userId " + userIdForAuth + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }

    public Flux<ProductResourceDto> getInstitutionProducts(String institutionId, String userId) {
        log.logInvokingExternalDownstreamService(SELFCARE_PA, "getInstitutions");
        return institutionsApi.getInstitutionUserProductsUsingGET(institutionId, userId)
                .doOnNext(productResourceDto -> log.info("getInstitutionProduct result: {}", productResourceDto))
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.logInvokationResultDownstreamFailed(SELFCARE_PA, CommonBaseClient.elabExceptionMessage(x));
                    log.error("getInstitutionProduct for institutionId " + institutionId + " response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new PnInternalException("Error getting product institutions", ERROR_CODE_EXTERNALREGISTRIES_INSTITUTIONSERROR, x));
                });
    }


}
