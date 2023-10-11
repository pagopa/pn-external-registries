package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_ROOT_ID_NOT_FOUND;

public class PnRootIdNotFoundException extends PnNotFoundException{
    public PnRootIdNotFoundException(String message) {
        super(message, "Non è stato trovato alcun Root Id", ERROR_CODE_EXTERNALREGISTRIES_ROOT_ID_NOT_FOUND);
    }
}
