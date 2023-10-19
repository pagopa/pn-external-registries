package it.pagopa.pn.external.registries.middleware.msclient.gpd;

import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.api.PaymentsApiApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.NotificationFeeUpdateModel;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@CustomLog
@Component
public class GpdClient extends OcpBaseClient {
    private final PaymentsApiApi paymentsApiApi;

    public GpdClient(PaymentsApiApi paymentsApiApi) {
        this.paymentsApiApi = paymentsApiApi;
    }
    
    public Mono<ResponseEntity<PaymentsModelResponse>> setNotificationCost(
            String creditorTaxId, 
            String noticeCode,
            String requestId, 
            Long notificationFee
    ) {
        //TODO Da valorizzare su commons il valore "GPD"
        log.logInvokingExternalService("GPD", "updateNotificationFee");

        String iuv = getIuvFromNoticeCode(noticeCode);
        NotificationFeeUpdateModel notificationFeeUpdateModel = new NotificationFeeUpdateModel();
        notificationFeeUpdateModel.setNotificationFee(notificationFee);
        paymentsApiApi.updateNotificationFee(creditorTaxId, iuv, notificationFeeUpdateModel);
        return paymentsApiApi.updateNotificationFeeWithHttpInfo(creditorTaxId, iuv, notificationFeeUpdateModel);
    }

    public String getIuvFromNoticeCode(String noticeCode){
        //per ottenere lo iuv dal notice code viene rimosso il primo carattere
        return noticeCode.substring(1);
    }
}
