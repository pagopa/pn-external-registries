package it.pagopa.pn.external.registries.exceptions;

public class PnException extends RuntimeException {



    private final String description;
    private final int status;

    public PnException(String message, String description) {
        this(message, description, 400, null);
    }

    public PnException(String message, String description, int status) {
        this(message, description, status, null);
    }

    public PnException(String message, String description, Throwable cause) {
        this(message, description, 400, cause);
    }

    public PnException(String message, String description, int status, Throwable cause) {
        super(message, cause);
        this.description = description;
        this.status = status;
    }

    public String getDescription() {
        return description;
    }
    public int getStatus(){ return status; }
}
