package it.pagopa.pn.external.registries.config;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.api.PaymentRequestsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery_reactive.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.api.PaymentsApiApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.InstitutionsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.UserApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.api.UserGroupApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.api.TimelineControllerApi;
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
        PaymentRequestsApi defaultApiClient(PnExternalRegistriesConfig config) {
            ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getCheckoutApiKey()).build());
            apiClient.setBasePath( config.getCheckoutApiBaseUrl() );
            return new PaymentRequestsApi( apiClient );
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
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient.buildWebClientBuilder(), config.getSelfcareusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcareusergroupBaseUrl());
            return new UserGroupApi(apiClient);
        }

        @Bean
        UserGroupApi userGroupPgApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient.buildWebClientBuilder(), config.getSelfcarepgusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcarepgusergroupBaseUrl());
            return new UserGroupApi(apiClient);
        }

        @Bean
        InstitutionsApi institutionsApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient.buildWebClientBuilder(), config.getSelfcareusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcareusergroupBaseUrl());
            return new InstitutionsApi(apiClient);
        }

        @Bean
        InstitutionsApi institutionsPgApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient.buildWebClientBuilder(), config.getSelfcarepgusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcarepgusergroupBaseUrl());
            return new InstitutionsApi(apiClient);
        }

        @Bean
        UserApi userPgApi() {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient(initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.ApiClient.buildWebClientBuilder(), config.getSelfcarepgusergroupApiKey()).build());
            apiClient.setBasePath(config.getSelfcarepgusergroupBaseUrl());
            return new UserApi(apiClient);
        }


    }

    @Configuration
    static class IOClient extends OcpBaseClient {

        @Bean
        it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi ioApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.ApiClient( initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.ApiClient.buildWebClientBuilder(), config.getIoApiKey()).build());
            apiClient.setBasePath( config.getIoBaseUrl() );

            return new it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi( apiClient );
        }

        @Bean
        it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi ioActApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.ApiClient( initWebClient(it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.ApiClient.buildWebClientBuilder(), config.getIoactApiKey()).build());
            apiClient.setBasePath( config.getIoBaseUrl() );

            return new it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi( apiClient );
        }
    }

    @Configuration
    static class GpdApis extends OcpBaseClient {
        @Bean
        PaymentsApiApi paymentsApiApiClient(PnExternalRegistriesConfig config) {
            it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.ApiClient apiClient = new
                    it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getGpdApiKey()).build());
            apiClient.setBasePath( config.getGpdApiBaseUrl() );
            return new PaymentsApiApi( apiClient );
        }
    }

    @Configuration
    static class DeliveryClient extends CommonBaseClient {
        @Bean
        InternalOnlyApi deliveryApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.delivery_reactive.v1.ApiClient( initWebClient(ApiClient.buildWebClientBuilder()));
            apiClient.setBasePath( config.getDeliveryBaseUrl() );
            return new InternalOnlyApi( apiClient );
        }
    }

    @Configuration
    static class TimelineServiceClient extends CommonBaseClient {
        @Bean
        TimelineControllerApi timelineServiceApi(PnExternalRegistriesConfig config) {
            var apiClient = new it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.ApiClient( initWebClient(ApiClient.buildWebClientBuilder()));
            apiClient.setBasePath( config.getTimelineServiceBaseUrl() );
            return new TimelineControllerApi ( apiClient );
        }
    }

}
