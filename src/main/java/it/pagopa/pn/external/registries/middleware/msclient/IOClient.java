package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class IOClient extends OcpBaseClient {

    private DefaultApi defaultApiClient;
    private final PnExternalRegistriesConfig config;

    public IOClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {

        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getIoApiKey()).build());
        apiClient.setBasePath( config.getIoBaseUrl() );

        this.defaultApiClient = new DefaultApi( apiClient );
    }

    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        return defaultApiClient.submitMessageforUserWithFiscalCodeInBody( message );
    }

    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        return defaultApiClient.getProfileByPOST( payload );
    }
}
