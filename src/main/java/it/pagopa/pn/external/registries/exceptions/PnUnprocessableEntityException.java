package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_DIGITALDOMICILENOTFOUND;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_IOUSERNOTFOUND;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnUnprocessableEntityException extends PnRuntimeException {

    public PnUnprocessableEntityException(String message, String errorcode) {
        super("Checkout UnprocessableEntityException", "Il servizio di checkout ha resituito UnprocessableEntity", HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_CODE_EXTERNALREGISTRIES_DIGITALDOMICILENOTFOUND, null, null);
    }

}