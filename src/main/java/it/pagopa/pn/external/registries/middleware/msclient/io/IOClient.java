package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class IOClient extends OcpBaseClient {

    private DefaultApi ioApi;
    private DefaultApi ioActivationMessageApi;
    private final PnExternalRegistriesConfig config;

    public IOClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {

        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getIoApiKey()).build());
        apiClient.setBasePath( config.getIoBaseUrl() );

        this.ioApi = new DefaultApi( apiClient );
        // è la stessa API ma con diverso API-KEY!
        apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getIoactApiKey()).build());
        apiClient.setBasePath( config.getIoBaseUrl() );

        this.ioActivationMessageApi = new DefaultApi( apiClient );

    }

    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.info("[enter] submitMessageforUserWithFiscalCodeInBody taxId={}", LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitMessageforUserWithFiscalCodeInBody taxId is not in whitelist, mocking IO response");
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }

        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<CreatedMessage> submitActivationMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.info("[enter] submitActivationMessageforUserWithFiscalCodeInBody taxId={}", LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitActivationMessageforUserWithFiscalCodeInBody taxId is not in whitelist, mocking IO response");
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }

        return ioActivationMessageApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitActivationMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.info("[enter] getProfileByPOST taxId={}", LogUtils.maskTaxId(payload.getFiscalCode()));

        if (!checkWhitelist(payload.getFiscalCode()))
        {
            log.warn("getProfileByPOST taxId is not in whitelist, mocking IO response");
            WebClientResponseException res = WebClientResponseException.create(404, "not found (mocked)", HttpHeaders.EMPTY, new byte[0], null);
            return Mono.error(res);
        }

        return ioApi.getProfileByPOST( payload );
    }

    private boolean checkWhitelist(String taxId)
    {
        return  (CollectionUtils.isEmpty(config.getIoWhitelist()) || config.getIoWhitelist().get(0).equals("*") || config.getIoWhitelist().contains(taxId));
    }
}
