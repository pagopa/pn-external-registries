package it.pagopa.pn.ext.registries.rest.utils;

import it.pagopa.pn.ext.registries.common.exceptions.PnInternalException;
import it.pagopa.pn.ext.registries.common.model.ProblemDto;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintViolationException;

public class HandleInternal {
    private HandleInternal(){}
    
    public static ResponseEntity<ProblemDto> handleInternalException(PnInternalException ex, Integer statusError){

        return ResponseEntity.badRequest()
                .body(ProblemDto.builder()
                        .title("Bad Request")
                        .detail(ex.getMessage())
                        .status(statusError)
                        .errors(null)
                        .build());
    }
}