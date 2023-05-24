package it.pagopa.pn.external.registries.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.api.UserGroupApi;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MsClientConfig {

    @Configuration
    static class CheckoutApis extends OcpBaseClient {

        @Bean
        DefaultApi defaultApiClient(PnExternalRegistriesConfig config) {
            ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getCheckoutApiKey()).build());
            apiClient.setBasePath( config.getCheckoutApiBaseUrl() );
            return new DefaultApi( apiClient );
        }

        //checkout ha una base-url diversa per il carrello
        @Bean
        DefaultApi defaultApiClientCartCheckout(PnExternalRegistriesConfig config) {
            ApiClient apiClientCartCheckout = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
            apiClientCartCheckout.setBasePath(config.getCheckoutCartApiBaseUrl());
            return new DefaultApi(apiClientCartCheckout);
        }

    }

    @Configuration
    @RequiredArgsConstructor
    static class SelfcareApis extends OcpBaseClient {

        private static final String HEADER_SELFCARE_UID = "x-selfcare-uid";

        private final PnExternalRegistriesConfig config;

        @Override
        protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey) {
            return super.initWebClient(builder, apiKey)
                    .defaultHeader(HEADER_SELFCARE_UID, config.getSelfcareusergroupUid());
        }

        @Bean
        UserGroupApi userGroupPaApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.ApiClient.buildWebClientBuilder(), config.getSelfcareusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcareusergroupBaseUrl());
            return new UserGroupApi(apiClient);
        }

        @Bean
        UserGroupApi userGroupPgApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.ApiClient.buildWebClientBuilder(), config.getSelfcarepgusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcarepgusergroupBaseUrl());
            return new UserGroupApi(apiClient);
        }

    }

    @Configuration
    static class IOClient extends OcpBaseClient {

        @Bean
        it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi ioApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient( initWebClient(it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient.buildWebClientBuilder(), config.getIoApiKey()).build());
            apiClient.setBasePath( config.getIoBaseUrl() );

            return new it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi( apiClient );
        }

        @Bean
        it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi ioActApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient( initWebClient(it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient.buildWebClientBuilder(), config.getIoactApiKey()).build());
            apiClient.setBasePath( config.getIoBaseUrl() );

            return new it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi( apiClient );
        }
    }

    @Configuration
    static class CommonBaseClients extends CommonBaseClient {

        @Bean
        InternalOnlyApi pnDeliveryApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.ApiClient( initWebClient( it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.ApiClient.buildWebClientBuilder() ) );
            apiClient.setBasePath( config.getDeliveryBaseUrl() );
            return new InternalOnlyApi( apiClient );
        }

        @Bean
        TimelineAndStatusApi pnDeliveryPushApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.ApiClient( initWebClient( it.pagopa.pn.external.registries.generated.openapi.deliverypush.client.v1.ApiClient.buildWebClientBuilder() ) );
            apiClient.setBasePath( config.getDeliveryPushBaseUrl() );
            return new TimelineAndStatusApi( apiClient );
        }
    }

}
