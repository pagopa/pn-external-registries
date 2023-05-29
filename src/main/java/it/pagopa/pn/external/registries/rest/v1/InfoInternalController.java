package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InternalOnlyApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupStatusDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class InfoInternalController implements InternalOnlyApi {

    private final InfoSelfcareGroupsService infoSelfcareService;

    public InfoInternalController(InfoSelfcareGroupsService infoSelfcareService) {
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
    public Mono<ResponseEntity<Flux<PaGroupDto>>> getAllGroupsPrivate(String institutionId, PaGroupStatusDto statusFilter, ServerWebExchange exchange) {
        log.debug("getAllGroups institutionId={}", institutionId);
        // first argument is the id of the current user logged in -> because we need the all groups independently from the user, it is passed as null
        // second argument is the id of the current PA
        return infoSelfcareService.getPaGroups(null, institutionId, null, statusFilter)
                .collectList()
                .map(paGroupDtos ->  ResponseEntity.ok(Flux.fromIterable(paGroupDtos)));
    }

    /**
     * GET /ext-registry-private/pg/v1/groups-all : Retrieve all groups of the current PG defined in Self Care
     * PG can use groups to better organize work in its organization. Return all the groups of the current PG
     *
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param statusFilter Se valorizzato indica di tornare solo i gruppi nello stato passato (optional)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PgGroupDto>>> getAllPgGroupsPrivate(String xPagopaPnCxId, PgGroupStatusDto statusFilter, ServerWebExchange exchange) {
        log.debug("getAllPgGroups institutionId={}", xPagopaPnCxId);
        return infoSelfcareService.getPgGroups(null, xPagopaPnCxId, null, statusFilter)
                .collectList()
                .map(pgGroupDtos -> ResponseEntity.ok(Flux.fromIterable(pgGroupDtos)));
    }
}
