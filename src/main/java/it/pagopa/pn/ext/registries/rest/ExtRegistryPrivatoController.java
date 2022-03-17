package it.pagopa.pn.ext.registries.rest;

import it.pagopa.pn.ext.registries.common.exceptions.PnInternalException;
import it.pagopa.pn.ext.registries.common.model.ProblemDto;
import it.pagopa.pn.ext.registries.privati.api.InfoDomicileApi;
import it.pagopa.pn.ext.registries.privati.model.AnalogDomicileDto;
import it.pagopa.pn.ext.registries.privati.model.DigitalDomicileDto;
import it.pagopa.pn.ext.registries.privati.model.RecipientTypeDto;
import it.pagopa.pn.ext.registries.rest.utils.HandleInternal;
import it.pagopa.pn.ext.registries.rest.utils.HandleValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

@RestController
@Slf4j
public class ExtRegistryPrivatoController implements InfoDomicileApi {

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
    public ResponseEntity<AnalogDomicileDto> getOneAnalogDomicile(RecipientTypeDto recipientType, UUID opaqueId) {
        log.info("getOneAnalogDomicile - recipientType: {} - opaqueId: {}", recipientType, opaqueId);

        if (RecipientTypeDto.PF.equals(recipientType)) {
            AnalogDomicileDto dto = new AnalogDomicileDto();
            dto.setAddress("ImmediateResponse(OK)");
            dto.setCap("40100");
            dto.setMunicipality("Bologna");
            dto.setProvince("BO");
            return ResponseEntity.ok(dto);
        } else {
            String msg = String.format("recipientType '%s' not found", recipientType);
            log.info("getOneAnalogDomicile - {}", msg);
            throw new PnInternalException(msg);
        }
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
    public ResponseEntity<DigitalDomicileDto> getOneDigitalDomicile(RecipientTypeDto recipientType, UUID opaqueId) {
        log.info("getOneDigitalDomicile - recipientType: {} - opaqueId: {}", recipientType, opaqueId);
        if (RecipientTypeDto.PF.equals(recipientType)) {
            DigitalDomicileDto dto = new DigitalDomicileDto();
            dto.setAddress("nome.cognome.1@works.pec.it");
            dto.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.PEC);
            return ResponseEntity.ok(dto);
        } else {
            String msg = String.format("recipientType '%s' not found", recipientType);
            throw new PnInternalException(msg);
        }
    }

    @ExceptionHandler({PnInternalException.class})
    public ResponseEntity<ProblemDto> handleInternalException(PnInternalException ex){
        return HandleInternal.handleInternalException(ex, HttpStatus.BAD_REQUEST.value() );
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ProblemDto> handleValidationException(ConstraintViolationException ex){
        return HandleValidation.handleValidationException(ex, HttpStatus.BAD_REQUEST.value() );
    }


}
