package it.pagopa.pn.external.registries.services;


import it.pagopa.pn.commons.exceptions.PnRuntimeException;
//import it.pagopa.pn.external.registries.dto.PageablePaSummaryResponseDto;
import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.mapper.*;
import it.pagopa.pn.external.registries.exceptions.PnRootIdNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.RootSenderIdResponseDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
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

  public Mono<PaSummaryExtendedResponseDto> extendedListOnboardedPaByName(String paNameFilter, Boolean onlyChildren, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    if (paNameFilter == null)
      paNameFilter = "";

    log.info("pagedListOnboardedPaByName - paNameFilter={} - onlyChildren={} - page={} - size={}", paNameFilter, onlyChildren, page, size);
    return onboardInstitutionFulltextSearchHelper.extendedFullTextSearch(paNameFilter, onlyChildren)
            .collectList()
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
