package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
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

    private final InfoSelfcareGroupsService infoSelfcareGroupsService;
    private final InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    public InfoPaController(InfoSelfcareInstitutionsService infoSelfcareInstitutionsService, InfoSelfcareGroupsService infoSelfcareGroupsService) {
        this.infoSelfcareGroupsService = infoSelfcareGroupsService;
        this.infoSelfcareInstitutionsService = infoSelfcareInstitutionsService;
    }

    @Override
    public Mono<ResponseEntity<Flux<PaSummaryDto>>> getManyPa(List<String> ids,  final ServerWebExchange exchange) {
        log.debug("getManyPa - ids={}", ids);
        return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareInstitutionsService.listOnboardedPaByIds( ids)));
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
        log.debug("getOnePa - id={}", id);
        return infoSelfcareInstitutionsService.getOnePa(id)
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
        log.debug("listOnboardedPa - paNameFilter={} ids={}", paNameFilter, ids);
        if( ids == null || ids.isEmpty() ) {
            return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareInstitutionsService.listOnboardedPaByName(paNameFilter)));
        }
        else {
            return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareInstitutionsService.listOnboardedPaByIds( ids)));
        }
    }

    /**
     * GET /ext-registry/pa/v1/groups : Retrieve the groups defined in Self Care of the current user
     * Used by the new notification page
     *
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PaGroupDto>>> getGroups(String xPagopaPnUid, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, PaGroupStatusDto statusFilter, ServerWebExchange exchange) {
        log.debug("getGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareGroupsService.getGroups(xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter)));
    }


    /**
     * GET /ext-registry-b2b/pa/v1/groups : Retrieve the groups defined in Self Care of the current user
     * Used by the new notification page
     *
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Flux<PaGroupDto>>> getGroupsB2B(String xPagopaPnUid, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, PaGroupStatusDto statusFilter, ServerWebExchange exchange) {
        log.debug("getGroupsB2B - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        return Mono.fromSupplier(() -> ResponseEntity.ok(infoSelfcareGroupsService.getGroups(xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter)));
    }
}
