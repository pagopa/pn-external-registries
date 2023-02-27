package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.dto.PaymentEventPagoPa;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class DeliveryClient extends OcpBaseClient {

    private InternalOnlyApi pnDeliveryApi;

    private final PnExternalRegistriesConfig config;

    public DeliveryClient(PnExternalRegistriesConfig config) { this.config = config; }

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient( initWebClient( ApiClient.buildWebClientBuilder() ) );
        apiClient.setBasePath( config.getDeliveryBaseUrl() );
        this.pnDeliveryApi = new InternalOnlyApi( apiClient );
    }

    public Mono<Void> paymentEventPagoPaPrivate(PaymentEventPagoPa paymentEventPagoPa) {
        return pnDeliveryApi.paymentEventPagoPaPrivate( paymentEventPagoPa );
    }
}
