package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.dto.ProbableSchedulingAnalogDateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class DeliveryPushClient extends CommonBaseClient {

    private TimelineAndStatusApi pnDeliveryPushApi;

    private final PnExternalRegistriesConfig config;

    public DeliveryPushClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient( initWebClient( ApiClient.buildWebClientBuilder() ) );
        apiClient.setBasePath( config.getDeliveryPushBaseUrl() );
        this.pnDeliveryPushApi = new TimelineAndStatusApi( apiClient );
    }

    public Mono<ResponseEntity<ProbableSchedulingAnalogDateResponse>> getSchedulingAnalogDateWithHttpInfo(String iun, String recipientId) {
        return pnDeliveryPushApi.getSchedulingAnalogDateWithHttpInfo(iun, recipientId);
    }
}
