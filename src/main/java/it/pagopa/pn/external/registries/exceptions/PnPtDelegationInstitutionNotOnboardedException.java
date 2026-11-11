package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED;

public class PnPtDelegationInstitutionNotOnboardedException extends PnRuntimeException {

    public PnPtDelegationInstitutionNotOnboardedException(String institutionId) {
        super("Institution not onboarded institutionId=" + institutionId, "Institution not onboarded institutionId=" + institutionId, HttpStatus.INTERNAL_SERVER_ERROR.value(), ERROR_CODE_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED, null, null);
    }

}
