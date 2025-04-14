package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnCheckoutUnauthorizedException extends PnRuntimeException {
    public PnCheckoutUnauthorizedException(String message,String errorcode) {
        super(message, message, HttpStatus.UNAUTHORIZED.value(), errorcode, null, null);
    }
}
