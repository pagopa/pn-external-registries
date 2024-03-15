package it.pagopa.pn.external.registries.middleware.msclient.io;

import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import it.pagopa.pn.external.registries.springbootcfg.SpringAnalyzerActivation;
import lombok.CustomLog;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.IO;

@CustomLog
class IOClient extends OcpBaseClient {

    protected final DefaultApi ioApi;
    private final PnExternalRegistriesConfig config;
    String ioMode;
    private final MeterRegistry meterRegistry;


    public IOClient(PnExternalRegistriesConfig config, DefaultApi ioApi, String ioMode, MeterRegistry meterRegistry) {
        this.config = config;
        this.ioApi = ioApi;
        this.ioMode = ioMode;
        this.meterRegistry = meterRegistry;
    }


    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.logInvokingExternalDownstreamService(IO, "submitMessageforUserWithFiscalCodeInBody");
        log.debug("[enter] submitMessageforUserWithFiscalCodeInBody ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitMessageforUserWithFiscalCodeInBody taxId {} is not in whitelist, mocking IO response", LogUtils.maskTaxId(message.getFiscalCode()));
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message )
                .map(response-> {
                    this.meterRegistry.get(SpringAnalyzerActivation.IO_SENT_SUCCESSFULLY).counter().increment();
                    return response;
                })
                .onErrorResume(throwable -> {
                    log.logInvokationResultDownstreamFailed(IO, elabExceptionMessage(throwable));
                    log.error("error submitMessageforUserWithFiscalCodeInBody ioMode={} message={}", ioMode, elabExceptionMessage(throwable), throwable);
                    this.meterRegistry.get(SpringAnalyzerActivation.IO_SENT_FAILURE).counter().increment();

                    return Mono.error(throwable);
        });
    }


    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.logInvokingExternalDownstreamService(IO, "getProfileByPOST");
        log.debug("[enter] getProfileByPOST ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(payload.getFiscalCode()));

        if (!checkWhitelist(payload.getFiscalCode()))
        {
            log.warn("getProfileByPOST taxId {} is not in whitelist, mocking IO response", LogUtils.maskTaxId(payload.getFiscalCode()));
            WebClientResponseException res = WebClientResponseException.create(404, "not found (mocked)", HttpHeaders.EMPTY, new byte[0], null);
            return Mono.error(res);
        }

        return ioApi.getProfileByPOST( payload ).doOnError(throwable -> {
            if (throwable instanceof NotFound){
                log.logInvokationResultDownstreamNotFound(IO, elabExceptionMessage(throwable));
            }else {
                log.logInvokationResultDownstreamFailed(IO, elabExceptionMessage(throwable));
            }
        });
    }

    protected boolean checkWhitelist(String taxId)
    {
        return  (CollectionUtils.isEmpty(config.getIoWhitelist()) || config.getIoWhitelist().get(0).equals("*") || config.getIoWhitelist().contains(taxId));
    }


}
