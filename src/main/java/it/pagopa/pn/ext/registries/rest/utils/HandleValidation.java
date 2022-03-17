package it.pagopa.pn.ext.registries.rest.utils;

import it.pagopa.pn.ext.registries.common.model.ProblemDto;
import it.pagopa.pn.ext.registries.common.model.ProblemErrorDto;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

public class HandleValidation {
    private HandleValidation(){}
    
    public static ResponseEntity<ProblemDto> handleValidationException(ConstraintViolationException ex, Integer statusError){
        List<ProblemErrorDto> listErrorDto = ex.getConstraintViolations().stream()
                .map(msg ->
                        ProblemErrorDto.builder()
                                .detail(msg.getMessage())
                                .element(msg.getPropertyPath()!= null ? msg.getPropertyPath().toString() : "")
                                .code(null)
                                .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(ProblemDto.builder()
                        .title("Validation Error")
                        .status(statusError)
                        .errors(listErrorDto)
                        .build());
    }
}