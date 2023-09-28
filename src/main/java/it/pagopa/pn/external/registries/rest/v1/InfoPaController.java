package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
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
        return infoSelfcareInstitutionsService.listOnboardedPaByIds( ids)
                .collectList()
                .map(paSummaryDtos -> ResponseEntity.ok(Flux.fromIterable(paSummaryDtos)));
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
            return infoSelfcareInstitutionsService.listOnboardedPaByName(paNameFilter)
                    .collectList()
                    .map(paSummaryDtos -> ResponseEntity.ok(Flux.fromIterable(paSummaryDtos)));
        }
        else {
            return infoSelfcareInstitutionsService.listOnboardedPaByIds( ids)
                    .collectList()
                    .map(paSummaryDtos -> ResponseEntity.ok(Flux.fromIterable(paSummaryDtos)));
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
        return infoSelfcareGroupsService.getPaGroups(xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter)
                .collectList()
                .map(paGroupDtos -> ResponseEntity.ok(Flux.fromIterable(paGroupDtos)));
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

        return infoSelfcareGroupsService.getPaGroups(xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter)
                .collectList()
                .map(paGroupDtos -> ResponseEntity.ok(Flux.fromIterable(paGroupDtos)));
    }

    @Override
    public Mono<ResponseEntity<Flux<InstitutionResourceDto>>> getInstitutions(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails, ServerWebExchange exchange) {
        log.debug("getPaInstitutions - xPagopaPnUid={} xPagopaPnCxType={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
        return infoSelfcareInstitutionsService.listInstitutionByCurrentUser(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails)
                .collectList()
                .map(institutionResourceDtos -> ResponseEntity.ok(Flux.fromIterable(institutionResourceDtos)));
    }

    @Override
    public Mono<ResponseEntity<Flux<ProductResourceDto>>> getInstitutionsProducts(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnSrcCh, String institutionId, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails, ServerWebExchange exchange) {
        log.debug("getPaInstitutions - institutionId={} xPagopaPnUid={} xPagopaPnCxType={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", institutionId, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);

        return infoSelfcareInstitutionsService.listProductsByInstitutionAndCurrentUser(institutionId, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails)
                .collectList()
                .map(productResourceDtos -> ResponseEntity.ok(Flux.fromIterable(productResourceDtos)));

    }
}
