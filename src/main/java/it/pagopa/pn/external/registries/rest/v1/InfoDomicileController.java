package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.api.v1.mock.InfoDomicilieImpl;
import it.pagopa.pn.external.registries.exceptions.PnException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.api.InfoDomicileApi;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.ProblemDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

@RestController
@Slf4j
public class InfoDomicileController implements InfoDomicileApi {

    /**
     * GET /ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/analog : Read the analog domicile of a notification recipient
     * Read the analog domicile of a notification recipient. Il destinatario  può essere una persona fisica o una persona giuridica.
     *
     * @param recipientType il tipo del destinatario (required)
     * @param opaqueId Identificativo universale univoco del destinatario (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AnalogDomicileDto>> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        log.debug("getOneAnalogDomicile - recipientType = {} - opaqueId = {}", recipientType, opaqueId);

        return InfoDomicilieImpl.getOneAnalogDomicile(recipientType, opaqueId, exchange);
    }

    /**
     * GET /ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/digital : Read the digital domicile of a notification recipient
     * Read the digital domicile of a notification recipient. Il destinatario  può essere una persona fisica o una persona giuridica.
     *
     * @param recipientType il tipo del destinatario (required)
     * @param opaqueId Identificativo universale univoco del destinatario (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<DigitalDomicileDto>> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId, ServerWebExchange exchange) {
        log.debug("getOneDigitalDomicile - recipientType = {} - opaqueId = {}", recipientType, opaqueId);

        return InfoDomicilieImpl.getOneDigitalDomicile(recipientType, opaqueId, exchange);
    }

    // catch exception when recipientType = PG (only PF is allowed)
    @ExceptionHandler({PnException.class})
    public ResponseEntity<ProblemDto> handleInternalException(PnException ex) {
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle("Bad Request");
        p.setDetail(ex.getMessage());
        p.setErrors(null);

        return ResponseEntity.badRequest().body(p);
    }

    // catch invalid opaqueId
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ProblemDto> handleValidationException(ConstraintViolationException ex) {
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle("Validation Error");
        p.setDetail(ex.getMessage());
        p.setErrors(null);

        return ResponseEntity.badRequest().body(p);
    }

    // catch invalid recipientType (PF or PG)
    @ExceptionHandler({TypeMismatchException.class})
    public ResponseEntity<ProblemDto> handleIllegalArgumentException(TypeMismatchException ex) {
        ProblemDto p = new ProblemDto();
        p.setStatus(HttpStatus.BAD_REQUEST.value());
        p.setTitle("Validation Error");
        p.setDetail(ex.getRootCause() != null?ex.getRootCause().getMessage():ex.getMessage());
        p.setErrors(null);

        return ResponseEntity.badRequest().body(p);
    }
}
