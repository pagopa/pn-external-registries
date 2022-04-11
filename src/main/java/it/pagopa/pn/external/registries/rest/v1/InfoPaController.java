package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.api.v1.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
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
import java.util.List;


@RestController
@Slf4j
public class InfoPaController implements InfoPaApi {

    /**
     * GET /ext-registry/pa/v1/activated-on-pn/{id} : Retrieve detailed information about one PA
     * Used by the Notification detail page
     *
     * @param id The identifier of one PA (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PaInfoDto>> getOnePa(String id, ServerWebExchange exchange) {
        log.debug("getOnePa - id = {}", id);
        return InfoPapiImpl.getOnePa(id, exchange);
    }

    /**
     * GET /ext-registry/pa/v1/activated-on-pn : List PA that use PN
     * Use with API to implement PA choose in domicile and mandate creation pages.
     *
     * @param paNameFilter Se valorizzato indica (optional)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PaSummaryDto>>> listOnboardedPa(String paNameFilter, List<String> ids, ServerWebExchange exchange) {
        log.debug("listOnboardedPa - paNameFilter = {}", paNameFilter);
        if( ids == null || ids.isEmpty() ) {
            return InfoPapiImpl.listOnboardedPa(paNameFilter, exchange);
        }
        else {
            return InfoPapiImpl.listOnboardedPa( ids, exchange);
        }
    }

    // catch id not found (c_f205 only allowed)
    @ExceptionHandler({PnInternalException.class})
    public ResponseEntity<ProblemDto> handleInternalException(PnInternalException ex){
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle("Bad Request");
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
