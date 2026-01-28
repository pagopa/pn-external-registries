package it.pagopa.pn.external.registries.services;


import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.exceptions.PnRootIdNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.mapper.*;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import it.pagopa.pn.external.registries.services.helpers.impl.OnboardInstitutionFulltextSearchHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareInstitutionsService {

  private final OnboardInstitutionsDao onboardInstitutionsDao;
  private final OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper;
  private final SelfcarePaInstitutionClient selfcarePaInstitutionClient;

  public InfoSelfcareInstitutionsService(OnboardInstitutionsDao onboardInstitutionsDao,
                                         OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper,
                                         SelfcarePaInstitutionClient selfcarePaInstitutionClient) {
    this.onboardInstitutionsDao = onboardInstitutionsDao;
    this.onboardInstitutionFulltextSearchHelper = onboardInstitutionFulltextSearchHelper;
    this.selfcarePaInstitutionClient = selfcarePaInstitutionClient;
  }

  public Mono<PaInfoDto> getOnePa(String id) throws PnRuntimeException {
    log.info("getOnePa - id={}", id);
    return onboardInstitutionsDao.get(id)
            .switchIfEmpty(Mono.error(new PnPANotFoundException()))
            .map(OnboardInstitutionEntityToPaInfoDto::toDto);
  }

  public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
    if (paNameFilter == null)
      paNameFilter = "";

    log.info("listOnboardedPaByName - paNameFilter={}", paNameFilter);
    return onboardInstitutionFulltextSearchHelper.fullTextSearch(paNameFilter);
  }

  /**
   * Retrieves a paginated list of onboarded public administrations (PA) that match the given name filter.
   * This method supports pagination and filtering based on whether only child institutions should be included.
   *
   * The method works as follows:
   * - Calls the {@code extendedFullTextSearch} method of the helper class to perform a full-text search on onboarded institutions.
   * - Buffers the results in chunks of the specified {@code size} per page.
   * - Retrieves the requested page using {@code elementAt(page - 1, List.of())}, returning an empty list if the page does not exist.
   * - Converts the list of results into a paginated response using a mapper.
   *
   * @param paNameFilter The name or part of the name of the institution to filter (optional, can be empty).
   * @param onlyChildren If {@code true}, retrieves only child institutions; otherwise, retrieves both parents and children.
   * @param page The page number to retrieve (1-based index).
   * @param size The number of elements per page.
   * @return A {@link Mono} emitting a {@link PaSummaryExtendedResponseDto} containing the paginated list of results.
   */
  public Mono<PaSummaryExtendedResponseDto> extendedListOnboardedPaByName(String paNameFilter, Boolean onlyChildren, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    if (paNameFilter == null)
      paNameFilter = "";

    log.info("pagedListOnboardedPaByName - paNameFilter={} - onlyChildren={} - page={} - size={}", paNameFilter, onlyChildren, page, size);
    return onboardInstitutionFulltextSearchHelper.extendedFullTextSearch(paNameFilter, onlyChildren)
            .buffer(size)
            .elementAt(page - 1, List.of())
            .map(list -> OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toPaginationPaSummaryExtended(pageable, list))
            .map(OnboardInstitutionEntityToPaSummaryExtendedDtoMapper::toPageableResponseExtended);
  }

  public Flux<InstitutionResourcePNDto> listInstitutionByCurrentUser(String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
    log.info("listInstitutionByCurrentUser - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
    return selfcarePaInstitutionClient.getInstitutions(xPagopaPnUid)
            .map(InstitutionsToInstitutionPNDtoMapper::toDto);
  }

  public Flux<ProductResourcePNDto> listProductsByInstitutionAndCurrentUser(String institutionId, String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
    log.info("listProductsByInstitutionAndCurrentUser - institutionId={} xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", institutionId, xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
    return selfcarePaInstitutionClient.getInstitutionProducts(institutionId, xPagopaPnUid)
            .map(ProductToProductPNDtoMapper::toDto);
  }

  public Flux<PaSummaryDto> listOnboardedPaByIds(List<String> ids) {
    log.info("listOnboardedPaByIds - ids={}", ids);
    return Flux.fromIterable(ids)
            .flatMap(onboardInstitutionsDao::get)
            .map(OnboardInstitutionEntityToPaSummaryDto::toDto);
  }

  public Mono<RootSenderIdResponseDto> getRootId(String id) throws PnRuntimeException {
    log.info("getRootId - id={}", id);
    return onboardInstitutionsDao.get(id)
        .switchIfEmpty(Mono.error(new PnRootIdNotFoundException(String.format("no root id for sender id=%s", id))))
        .map( response -> new RootSenderIdResponseDto().rootId(response.getRootId()));
  }

  public Flux<String> filterOutRootIds(List<String> ids) {
    log.info("listFiltered - ids={}", ids);
    return onboardInstitutionsDao.filterOutRootIds(ids).map(OnboardInstitutionEntity::getInstitutionId);
  }

}
