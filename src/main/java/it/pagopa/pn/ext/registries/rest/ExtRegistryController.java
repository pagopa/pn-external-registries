package it.pagopa.pn.ext.registries.rest;

import it.pagopa.pn.ext.registries.common.exceptions.PnInternalException;
import it.pagopa.pn.ext.registries.common.model.ProblemDto;
import it.pagopa.pn.ext.registries.pa.api.InfoPaApi;
import it.pagopa.pn.ext.registries.pa.model.PaContactsDto;
import it.pagopa.pn.ext.registries.pa.model.PaInfoDto;
import it.pagopa.pn.ext.registries.rest.utils.HandleInternal;
import it.pagopa.pn.ext.registries.rest.utils.HandleValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class ExtRegistryController implements InfoPaApi {
    public static final Integer EXT_REGISTRY_VALIDATION_ERROR_STATUS = 1;

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
    public ResponseEntity<PaInfoDto> getOnePa(String id) {

        log.info("getOnePa - id: {}", id);

        if ("c_f205".equals(id)) {
            PaContactsDto pac = new PaContactsDto("protocollo@postacert.comune.milano.it",
                    "protocollo@comune.milano.it",
                    "340000333",
                    URI.create("www.comune.milano.it"));
            PaInfoDto dto = new PaInfoDto(id,"Comune di Milano","01199250158",pac);
            dto.setGeneralContacts(pac);

            return ResponseEntity.ok(dto);
        } else {
            String msg = String.format("id '%s' not found", id);
            log.info("getOnePa - {}", msg);
            throw new PnInternalException(msg);
        }
    }

    /**
     * GET /ext-registry/pa/v1/activated-on-pn : List PA that use PN
     * Use with API to implement PA choose in domicile and mandate creation pages.
     *
     * @param paNameFilter Se valorizzato indica (optional)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500
     *
     *         se "c_f205" inizia per paNameFilter allora ritorno
     */
    @Override
    public ResponseEntity<List<Object>> listOnboardedPa(String paNameFilter) {

        log.info("listOnboardedPa - paNameFilter: {}", paNameFilter);

        if (paNameFilter == null || "c_f205".startsWith(paNameFilter)) {
            PaContactsDto pac = new PaContactsDto("protocollo@postacert.comune.milano.it",
                    "protocollo@comune.milano.it",
                    "340000333",
                    URI.create("www.comune.milano.it"));
            PaInfoDto dto = new PaInfoDto("c_f205", "Comune di Milano", "01199250158", pac);
            dto.setGeneralContacts(pac);
            List<Object> list = Arrays.asList(dto);
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.ok(Arrays.asList());
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
