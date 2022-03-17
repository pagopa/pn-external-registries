package it.pagopa.pn.ext.registries.rest.utils;

import it.pagopa.pn.ext.registries.common.model.ProblemDto;
import org.springframework.http.ResponseEntity;

public class HandleIllegalArgument {
    private HandleIllegalArgument(){}
    
    public static ResponseEntity<ProblemDto> handleIllegalArgumentException(IllegalArgumentException ex, Integer statusError){

        return ResponseEntity.badRequest()
                .body(ProblemDto.builder()
                        .title("Bad Request")
                        .detail(ex.getMessage())
                        .status(statusError)
                        .errors(null)
                        .build());
    }
}