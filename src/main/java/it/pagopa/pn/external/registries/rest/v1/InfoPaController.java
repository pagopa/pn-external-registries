package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
public class InfoPaController implements InfoPaApi {

    private final InfoSelfcareGroupsService infoSelfcareGroupsService;
    private final InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;
    private final InfoSelfcareUserService infoSelfcareUserService;

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
     * GET /ext-registry/pa/v2/activated-on-pn : List all PA and relative children that use PN
     * Use with API to implement PA choose in domicile and mandate creation pages.
     *
     * @param paNameFilter Se valorizzato indica il nome o parte di esso da ricercare (optional)
     * @param onlyChildren Indica se restituire soltanto i figli
     * @param page Indica il numero della pagina (default: 1)
     * @param size Indica la quantità di elementi per pagina (default: 1)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PaSummaryExtendedResponseDto>> extendedListOnboardedPa(String paNameFilter, Boolean onlyChildren, Integer page, Integer size, ServerWebExchange exchange) {
        log.debug("extendedListOnboardedPa - paNameFilter={} onlyChildren={} page={} size={}", paNameFilter, onlyChildren, page, size);
        return infoSelfcareInstitutionsService.extendedListOnboardedPaByName(paNameFilter, onlyChildren, page, size)
                .map(ResponseEntity::ok);
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
    public Mono<ResponseEntity<Flux<InstitutionResourcePNDto>>> getInstitutions(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails, ServerWebExchange exchange) {
        log.debug("getPaInstitutions - xPagopaPnUid={} xPagopaPnCxType={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
        return infoSelfcareInstitutionsService.listInstitutionByCurrentUser(xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails)
                .collectList()
                .map(institutionResourceDtos -> ResponseEntity.ok(Flux.fromIterable(institutionResourceDtos)));
    }

    @Override
    public Mono<ResponseEntity<Flux<InstitutionResourcePNDto>>> getUserInstitutions(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails, ServerWebExchange exchange) {
        log.debug("getPaInstitutions - xPagopaPnUid={} xPagopaPnCxType={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
        return infoSelfcareUserService.listUserInstitutionByCurrentUser(xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails)
                .collectList()
                .map(institutionResourceDtos -> ResponseEntity.ok(Flux.fromIterable(institutionResourceDtos)));
    }

    @Override
    public Mono<ResponseEntity<Flux<ProductResourcePNDto>>> getInstitutionProducts(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnSrcCh, String institutionId, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails, ServerWebExchange exchange) {
        log.debug("getPaInstitutions - institutionId={} xPagopaPnUid={} xPagopaPnCxType={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", institutionId, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);

        return infoSelfcareInstitutionsService.listProductsByInstitutionAndCurrentUser(institutionId, xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails)
                .collectList()
                .map(productResourceDtos -> ResponseEntity.ok(Flux.fromIterable(productResourceDtos)));

    }
}
