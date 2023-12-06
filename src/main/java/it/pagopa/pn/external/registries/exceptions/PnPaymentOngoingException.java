package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import lombok.Getter;
import org.springframework.http.HttpHeaders;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PAYMENT_ONGOING;

@Getter
public class PnPaymentOngoingException extends PnInternalException {
    private final PaymentsModelResponse response;
    private final HttpHeaders headers;
    private final int statusCode;
    
    public PnPaymentOngoingException(PaymentsModelResponse response, HttpHeaders headers, int statusCode) {
        super("Payment ongoing", ERROR_CODE_EXTERNALREGISTRIES_PAYMENT_ONGOING);
        this.response = response;
        this.headers = headers;
        this.statusCode = statusCode;
    }
}
