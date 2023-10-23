package it.pagopa.pn.external.registries.middleware.msclient.gpd;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.api.PaymentsApiApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.NotificationFeeUpdateModel;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.GPD;

@CustomLog
@Component
public class GpdClient extends OcpBaseClient {
    private final PnExternalRegistriesConfig config;
    
    public GpdClient(PnExternalRegistriesConfig config) {
        this.config = config;
    }
    
    public Mono<ResponseEntity<PaymentsModelResponse>> setNotificationCost(
            String creditorTaxId, 
            String noticeCode,
            String requestId, 
            Long notificationFee
    ) {
        log.logInvokingExternalService(GPD, "updateNotificationFee");
        PaymentsApiApi paymentsApiApi = getPaymentsApiApi(config, requestId);

        String iuv = getIuvFromNoticeCode(noticeCode);
        NotificationFeeUpdateModel notificationFeeUpdateModel = new NotificationFeeUpdateModel();
        notificationFeeUpdateModel.setNotificationFee(notificationFee);
        paymentsApiApi.updateNotificationFee(creditorTaxId, iuv, notificationFeeUpdateModel);
        return paymentsApiApi.updateNotificationFeeWithHttpInfo(creditorTaxId, iuv, notificationFeeUpdateModel);
    }

    private String getIuvFromNoticeCode(String noticeCode){
        //iuv is noticeCode without first char
        return noticeCode.substring(1);
    }

    PaymentsApiApi getPaymentsApiApi(PnExternalRegistriesConfig config, String requestId) {
        ApiClient apiClient = new
                ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), config.getGpdApiKey(), requestId).build());
        apiClient.setBasePath( config.getGpdApiBaseUrl() );
        return new PaymentsApiApi( apiClient );
    }
}
