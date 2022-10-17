package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;


public class PnCheckoutServerErrorException extends PnRuntimeException {

    public PnCheckoutServerErrorException(String message, String errorcode) {
        super("Checkout server error", message, HttpStatus.INTERNAL_SERVER_ERROR.value(), errorcode, null, null);
    }

}

