package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;


public class PnCheckoutBadRequestException extends PnRuntimeException {

    public PnCheckoutBadRequestException(String message, String errorcode) {
        super(message, "", HttpStatus.BAD_REQUEST.value(), errorcode, null, null);
    }

}

