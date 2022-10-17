package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;


public class PnNotFoundException extends PnRuntimeException {

    public PnNotFoundException(String message, String description, String errorcode) {
        super(message, description, HttpStatus.NOT_FOUND.value(), errorcode, null, null);
    }

}
