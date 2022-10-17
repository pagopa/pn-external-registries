package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;


public class PnCheckoutInternalException extends PnRuntimeException {

    public PnCheckoutInternalException(String message, String errorcode) {
        super(message, "", HttpStatus.INTERNAL_SERVER_ERROR.value(), errorcode, null, null);
    }

}

