package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;

public class AdditionalLangException extends PnRuntimeException {

    public AdditionalLangException(String message, int status, String errorcode) {
        super(message, message, status, errorcode, message, null);
    }

}
