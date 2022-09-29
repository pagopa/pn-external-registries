package it.pagopa.pn.external.registries.middleware.msclient.io;

import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.ActivationPayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

@Component
@Slf4j
public class IOCourtesyMessageClient extends IOClient {

    public static final String IO_STATUS_ACTIVE = "ACTIVE";
    public static final String IO_STATUS_INACTIVE = "INACTIVE";


    public IOCourtesyMessageClient(PnExternalRegistriesConfig config)
    {
        super(config, config.getIoApiKey());
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

        if (!checkWhitelist(taxId))
        {
            log.warn("taxId is not in whitelist, mocking IO response");
            Activation res = new Activation();
            res.setVersion(1);
            res.setStatus(IO_STATUS_INACTIVE);
            res.setFiscalCode(taxId);
            return Mono.just(res);
        }

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

        if (!checkWhitelist(taxId))
        {
            log.warn("taxId is not in whitelist, mocking IO response");
            Activation res = new Activation();
            res.setVersion(1);
            res.setStatus(IO_STATUS_INACTIVE);
            res.setFiscalCode(taxId);
            return Mono.just(res);
        }

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
