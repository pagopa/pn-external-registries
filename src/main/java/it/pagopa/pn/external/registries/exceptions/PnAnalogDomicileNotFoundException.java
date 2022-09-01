package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_ANALOGDOMICILENOTFOUND;


public class PnAnalogDomicileNotFoundException extends PnRuntimeException {

    public PnAnalogDomicileNotFoundException() {
        super("Domicilio analogico non trovato", "Non è stata trovata nessun domicilio analogico", HttpStatus.NOT_FOUND.value(), ERROR_CODE_EXTERNALREGISTRIES_ANALOGDOMICILENOTFOUND, null, null);
    }

}
