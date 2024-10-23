package it.pagopa.pn.external.registries.exceptions;

public class PnPANotFoundException extends PnNotFoundException {

    public PnPANotFoundException(String code) {
        super("PA non trovata", "Non è stata trovata nessuna PA", code);
    }

}
