package it.pagopa.pn.external.registries.exceptions;

public class NotFoundException extends PnException {


    public NotFoundException() {
        super("Elemento non trovato", "Non sono stati trovati elementi", 404);
    }

}
