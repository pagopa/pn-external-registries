package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;


public class PnExternalRegistriesBadRequestException extends PnRuntimeException {

    public PnExternalRegistriesBadRequestException(String message, String description,String errorcode) {
        super(message, description, HttpStatus.BAD_REQUEST.value(), errorcode, null, null);

    }

}

