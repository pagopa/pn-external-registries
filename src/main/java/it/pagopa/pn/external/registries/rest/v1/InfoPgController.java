package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPgApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupStatusDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
public class InfoPgController implements InfoPgApi {

    private final InfoSelfcareGroupsService infoSelfcareGroupsService;

    public InfoPgController(InfoSelfcareGroupsService infoSelfcareGroupsService) {
        this.infoSelfcareGroupsService = infoSelfcareGroupsService;
    }

    /**
     * GET /ext-registry/pg/v1/groups : Retrieve group of current user defined in Self Care
     * PG can use groups to better organize work in its organization. Return the PgGroupList of the current user, or if the user isn&#39;t in some group, all the groups of the current PG
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param statusFilter Se valorizzato indica di tornare solo i gruppi nello stato passato (optional)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PgGroupDto>>> getPgGroups(String xPagopaPnUid,
                                                              String xPagopaPnCxId,
                                                              List<String> xPagopaPnCxGroups,
                                                              PgGroupStatusDto statusFilter,
                                                              ServerWebExchange exchange) {
        log.debug("getPgGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareGroupsService.getPgGroups(xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter)));
    }
}
