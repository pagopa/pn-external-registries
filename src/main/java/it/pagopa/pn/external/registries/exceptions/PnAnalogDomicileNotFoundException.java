package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_ANALOGDOMICILENOTFOUND;


public class PnAnalogDomicileNotFoundException extends PnNotFoundException {

    public PnAnalogDomicileNotFoundException() {
        super("Domicilio analogico non trovato", "Non è stata trovata nessun domicilio analogico", ERROR_CODE_EXTERNALREGISTRIES_ANALOGDOMICILENOTFOUND);
    }

}
