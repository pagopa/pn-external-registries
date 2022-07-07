package it.pagopa.pn.external.registries.middleware.msclient;

import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;

@Component
@Slf4j
public class IOClient extends OcpBaseClient {

    public static final String IO_STATUS_ACTIVE = "ACTIVE";
    public static final String IO_STATUS_INACTIVE = "INACTIVE";

    private DefaultApi ioApi;
    private final PnExternalRegistriesConfig config;

    public IOClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {

        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getIoApiKey()).build());
        apiClient.setBasePath( config.getIoBaseUrl() );

        this.ioApi = new DefaultApi( apiClient );
    }

    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        return ioApi.getProfileByPOST( payload );
    }

    /**
     * Crea (o aggiorna) lo stato in IO
     *
     * @param taxId taxId dell'utente
     * @param activated indica se attivato o disattivato
     *
     * @return void
     */
    public Mono<Activation> upsertServiceActivation(String taxId, boolean activated)
    {
        log.info("upsertServiceActivation taxId={} activated={}", LogUtils.maskTaxId(taxId), activated);

        ActivationPayload dto = new ActivationPayload();
        dto.setFiscalCode(taxId);
        dto.setStatus(activated? IO_STATUS_ACTIVE : IO_STATUS_INACTIVE);

        return ioApi.upsertServiceActivation(dto)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(throwable -> {
                    log.error("error upserting service activation message={}", elabExceptionMessage(throwable) , throwable);
                    return getServiceActivation(taxId);
                })
                .map(x -> {
                    log.info("upsertServiceActivation response taxid={} status={} serviceId={} version={}", LogUtils.maskTaxId(x.getFiscalCode()), x.getStatus(), x.getServiceId(), x.getVersion());
                    return x;
                });
    }

    /**
     * Ritorna lo stato lo stato in IO
     *
     * @param taxId internalId dell'utente
     *
     * @return void
     */
    public Mono<Activation> getServiceActivation(String taxId)
    {
        log.info("getServiceActivation taxId={}", LogUtils.maskTaxId(taxId));

        FiscalCodePayload dto = new FiscalCodePayload();
        dto.setFiscalCode(taxId);

        return ioApi.getServiceActivationByPOST(dto)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .map(x -> {
                    log.info("getServiceActivation response taxid={} status={} serviceId={} version={}", LogUtils.maskTaxId(x.getFiscalCode()), x.getStatus(), x.getServiceId(), x.getVersion());
                    return x;
                });
    }
}
