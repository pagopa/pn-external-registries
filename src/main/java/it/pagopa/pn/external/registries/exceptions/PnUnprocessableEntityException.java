package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnUnprocessableEntityException extends PnRuntimeException {

    public PnUnprocessableEntityException(String message, String errorcode) {
        super("UnprocessableEntityException", message, HttpStatus.UNPROCESSABLE_ENTITY.value(), errorcode, null, null);
    }

}