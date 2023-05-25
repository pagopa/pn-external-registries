package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@CustomLog
class IOClient extends OcpBaseClient {

    protected final DefaultApi ioApi;
    private final PnExternalRegistriesConfig config;
    String ioMode;

    public IOClient(PnExternalRegistriesConfig config, DefaultApi ioApi, String ioMode) {
        this.config = config;
        this.ioApi = ioApi;
        this.ioMode = ioMode;
    }


    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.logInvokingExternalService("IO", "submitMessageforUserWithFiscalCodeInBody");
        log.info("[enter] submitMessageforUserWithFiscalCodeInBody ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitMessageforUserWithFiscalCodeInBody taxId is not in whitelist, mocking IO response");
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }

        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody ioMode={} message={}", ioMode, elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }


    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.logInvokingExternalService("IO", "getProfileByPOST");
        log.info("[enter] getProfileByPOST ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(payload.getFiscalCode()));

        if (!checkWhitelist(payload.getFiscalCode()))
        {
            log.warn("getProfileByPOST taxId is not in whitelist, mocking IO response");
            WebClientResponseException res = WebClientResponseException.create(404, "not found (mocked)", HttpHeaders.EMPTY, new byte[0], null);
            return Mono.error(res);
        }

        return ioApi.getProfileByPOST( payload );
    }

    protected boolean checkWhitelist(String taxId)
    {
        return  (CollectionUtils.isEmpty(config.getIoWhitelist()) || config.getIoWhitelist().get(0).equals("*") || config.getIoWhitelist().contains(taxId));
    }
}
