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
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<CreatedMessage> submitActivationMessageforUserWithFiscalCodeInBody(NewMessage message) {
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitActivationMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        return ioApi.getProfileByPOST( payload );
    }

}
