package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareServiceMock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class InfoInternalPaController implements InternalOnlyApi {

    private final InfoSelfcareServiceMock infoSelfcareService;

    public InfoInternalPaController(InfoSelfcareServiceMock infoSelfcareService) {
        this.infoSelfcareService = infoSelfcareService;
    }

    /**
     * GET /ext-registry-private/pa/v1/groups-all : Retrieve all groups of the current PA defined in Self Care
     * Used by the Notification detail page and notification list page
     *
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PaGroupDto>>> getAllGroupsPrivate(String internalPaId, PaGroupStatusDto statusFilter, ServerWebExchange exchange) {
        log.debug("getAllGroups internalPaId={}", internalPaId);
        // first argument is the id of the current user logged in -> because we need the all groups independently from the user, it is passed as null
        // second argument is the id of the current PA
        return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareService.getGroups(null, internalPaId, null, statusFilter)));
    }

}
