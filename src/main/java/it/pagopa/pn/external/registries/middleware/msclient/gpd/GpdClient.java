package it.pagopa.pn.external.registries.middleware.msclient.gpd;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.api.PaymentsApiApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.NotificationFeeUpdateModel;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.GPD;

@CustomLog
@Component
@AllArgsConstructor
public class GpdClient extends OcpBaseClient {
    private final PaymentsApiApi paymentsApiApi;
    
    public Mono<ResponseEntity<PaymentsModelResponse>> setNotificationCost(
            String creditorTaxId, 
            String noticeCode,
            String requestId, 
            Long notificationFee
    ) {
        log.logInvokingExternalService(GPD, "updateNotificationFee");

        String iuv = getIuvFromNoticeCode(noticeCode);
        NotificationFeeUpdateModel notificationFeeUpdateModel = new NotificationFeeUpdateModel();
        notificationFeeUpdateModel.setNotificationFee(notificationFee);
        return paymentsApiApi.updateNotificationFeeWithHttpInfo(creditorTaxId, iuv, notificationFeeUpdateModel,requestId);
    }

    private String getIuvFromNoticeCode(String noticeCode){
        //iuv is noticeCode without first char
        return noticeCode.substring(1);
    }
}
