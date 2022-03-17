package it.pagopa.pn.ext.registries.common.exceptions;

public class PnInternalException extends RuntimeException {

    public PnInternalException(String message) {
        super(message);
    }

    public PnInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
