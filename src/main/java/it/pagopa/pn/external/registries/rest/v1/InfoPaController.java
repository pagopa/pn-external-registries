package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.api.v1.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@Slf4j
public class InfoPaController implements InfoPaApi {

    private final InfoPapiImpl infoPapi;

    public InfoPaController(InfoPapiImpl infoPapi) {
        this.infoPapi = infoPapi;
    }

    /**
     * GET /ext-registry-private/pa/v1/activated-on-pn/{id} : Retrieve detailed information about one PA
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
        return infoPapi.getOnePa(id)
                .map(m -> ResponseEntity.ok().body(m));
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
        log.debug("listOnboardedPa - paNameFilter = {} ids:{}", paNameFilter, ids);
        if( ids == null || ids.isEmpty() ) {

            return Mono.fromSupplier(() -> ResponseEntity.ok(infoPapi.listOnboardedPaByName(paNameFilter)));
        }
        else {
            return Mono.fromSupplier(() -> ResponseEntity.ok(infoPapi.listOnboardedPaByIds( ids)));
        }
    }

}
