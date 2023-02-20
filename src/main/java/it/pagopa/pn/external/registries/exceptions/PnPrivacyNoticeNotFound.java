package it.pagopa.pn.external.registries.exceptions;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PRIVACY_NOTICE_NOT_FOUND;

public class PnPrivacyNoticeNotFound extends PnNotFoundException {

    public PnPrivacyNoticeNotFound(String message) {
        super(message, "Non è stata trovata nessun Privacy Notice", ERROR_CODE_EXTERNALREGISTRIES_PRIVACY_NOTICE_NOT_FOUND);
    }

}
