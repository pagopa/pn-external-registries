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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

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
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<CreatedMessage> submitActivationMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.info("[enter] submitActivationMessageforUserWithFiscalCodeInBody taxId={}", LogUtils.maskTaxId(message.getFiscalCode()));
        return ioActivationMessageApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitActivationMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.info("[enter] getProfileByPOST taxId={}", LogUtils.maskTaxId(payload.getFiscalCode()));
        return ioApi.getProfileByPOST( payload );
    }

}
