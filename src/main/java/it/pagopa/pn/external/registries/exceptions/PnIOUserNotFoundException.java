package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_IOUSERNOTFOUND;


public class PnIOUserNotFoundException extends PnRuntimeException {

    public PnIOUserNotFoundException() {
        super("Utente IO non trovato", "Non è stata trovata nessuna utente IO", HttpStatus.NOT_FOUND.value(), ERROR_CODE_EXTERNALREGISTRIES_IOUSERNOTFOUND, null, null);
    }

}
