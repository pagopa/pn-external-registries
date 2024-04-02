package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnCheckoutNotFoundException  extends PnRuntimeException {

    public PnCheckoutNotFoundException(String message, String errorcode) {
        super("Checkout not found", message, HttpStatus.NOT_FOUND.value(), errorcode, null, null);
    }

}
