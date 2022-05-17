package it.pagopa.pn.external.registries.exceptions;

public class InternalErrorException extends PnException {


    public InternalErrorException() {
        super("Errore interno", "Errore applicativo interno", 500);
    }

}
