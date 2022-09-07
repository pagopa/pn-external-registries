package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND;


public class PnDigitalDomicileNotFoundException extends PnNotFoundException {

    public PnDigitalDomicileNotFoundException() {
        super("Domicilio digitale non trovato", "Non è stata trovata nessun domicilio digitale", ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND);
    }

}
