package it.pagopa.pn.external.registries.rest;

import it.pagopa.pn.external.registries.api.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.ProblemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;

@RestController
@Slf4j
public class InfoPaController implements InfoPaApi {
    @Override
    public Mono<ResponseEntity<PaInfoDto>> getOnePa(String id, ServerWebExchange exchange) {
        //return InfoPaApi.super.getOnePa(id, exchange);
        return InfoPapiImpl.getOnePa(id, exchange);
    }

    @Override
    public Mono<ResponseEntity<Flux<Object>>> listOnboardedPa(String paNameFilter, ServerWebExchange exchange) {
        //return InfoPaApi.super.listOnboardedPa(paNameFilter, exchange);
        return InfoPapiImpl.listOnboardedPa(paNameFilter, exchange);
    }

    // catch id not found (c_f205 only allowed)
    @ExceptionHandler({PnInternalException.class})
    public ResponseEntity<ProblemDto> handleInternalException(PnInternalException ex){
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle(null);
        p.setDetail(ex.getMessage());
        p.setErrors(null);

        return ResponseEntity.badRequest().body(p);
    }

    // catch invalid paNameFilter
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ProblemDto> handleValidationException(ConstraintViolationException ex){
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle("Validation Error");
        p.setDetail(ex.getMessage());
        p.setErrors(null);

        return ResponseEntity.badRequest().body(p);
    }

}
