package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_DIGITALDOMICILENOTFOUND;


public class PnPANotFoundException extends PnNotFoundException {

    public PnPANotFoundException(String code) {
        super("PA non trovata", "Non è stata trovata nessuna PA", code);
    }

}
