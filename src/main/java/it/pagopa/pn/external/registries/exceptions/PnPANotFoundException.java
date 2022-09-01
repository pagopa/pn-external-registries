package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_DIGITALDOMICILENOTFOUND;


public class PnPANotFoundException extends PnRuntimeException {

    public PnPANotFoundException() {
        super("PA non trovata", "Non è stata trovata nessuna PA", HttpStatus.NOT_FOUND.value(), ERROR_CODE_EXTERNALREGISTRIES_DIGITALDOMICILENOTFOUND, null, null);
    }

}
