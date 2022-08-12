package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionResourceDto;
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
import java.util.UUID;

@Component
@Slf4j
public class SelfcareInstitutionsClient extends OcpBaseClient {

    private static final String HEADER_SELFCARE_UID = "x-selfcare-uid";

    private InstitutionsApi institutionsApi;
    private final PnExternalRegistriesConfig config;

    public SelfcareInstitutionsClient(PnExternalRegistriesConfig config) {
        this.config = config; }

    @PostConstruct
    public void init() {
    // TODO implemetnare quando ci saranno le aPI corrette
    }

    @Override
    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){
        return super.initWebClient(builder, apiKey)
                .defaultHeader(HEADER_SELFCARE_UID,config.getSelfcareinstitutionsApiKey());
    }

    // TODO: implementare quando ci saranno le API corrette. I metodi dovrebbero essere cmq questi
    public Mono<InstitutionDto> getInstitution(String institutionId) throws WebClientResponseException {

        return institutionsApi.getInstitution(UUID.fromString(institutionId), config.getSelfcareinstitutionsApiKey())
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

        return institutionsApi.getInstitutionsUsingGET(config.getSelfcareinstitutionsPnProductId(), config.getSelfcareinstitutionsApiKey())
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(WebClientResponseException.class, x -> {
                    log.error("getInstitutions response error {}", x.getResponseBodyAsString(), x);
                    return Mono.error(new InternalErrorException());
                });
    }
}
