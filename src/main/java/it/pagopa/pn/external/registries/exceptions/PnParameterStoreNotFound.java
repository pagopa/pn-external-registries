package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PARAMETER_STORE_NOT_FOUND;

public class PnParameterStoreNotFound extends PnRuntimeException {

    public PnParameterStoreNotFound(String message) {
        super("Parameter Store not found", message, HttpStatus.INTERNAL_SERVER_ERROR.value(), ERROR_CODE_EXTERNALREGISTRIES_PARAMETER_STORE_NOT_FOUND, null, null);
    }

}
