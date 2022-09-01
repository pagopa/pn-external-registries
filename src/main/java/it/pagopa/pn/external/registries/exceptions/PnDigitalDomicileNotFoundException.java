package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND;


public class PnDigitalDomicileNotFoundException extends PnRuntimeException {

    public PnDigitalDomicileNotFoundException() {
        super("Domicilio digitale non trovato", "Non è stata trovata nessun domicilio digitale", HttpStatus.NOT_FOUND.value(), ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND, null, null);
    }

}
