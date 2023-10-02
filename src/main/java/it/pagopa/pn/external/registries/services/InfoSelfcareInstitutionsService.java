package it.pagopa.pn.external.registries.services;


import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.mapper.OnboardInstitutionEntityToPaInfoDto;
import it.pagopa.pn.external.registries.mapper.OnboardInstitutionEntityToPaSummaryDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import it.pagopa.pn.external.registries.services.helpers.OnboardInstitutionFulltextSearchHelper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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

  private final ModelMapper modelMapper;
  public InfoSelfcareInstitutionsService(OnboardInstitutionsDao onboardInstitutionsDao,
                                         OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper,
                                         SelfcarePaInstitutionClient selfcarePaInstitutionClient,
                                         ModelMapper modelMapper) {
    this.onboardInstitutionsDao = onboardInstitutionsDao;
    this.onboardInstitutionFulltextSearchHelper = onboardInstitutionFulltextSearchHelper;
    this.selfcarePaInstitutionClient = selfcarePaInstitutionClient;
    this.modelMapper = modelMapper;
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

  public Flux<InstitutionResourceDto> listInstitutionByCurrentUser(String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
    log.info("listInstitutionByCurrentUser - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
    return selfcarePaInstitutionClient.getInstitutions(xPagopaPnUid).map(
            institutionResourceDto -> modelMapper.map(institutionResourceDto, InstitutionResourceDto.class)
    );
  }

  public Flux<ProductResourceDto> listProductsByInstitutionAndCurrentUser(String institutionId, String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
    log.info("listProductsByInstitutionAndCurrentUser - institutionId={} xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", institutionId, xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
    return selfcarePaInstitutionClient.getInstitutionProducts(institutionId, xPagopaPnUid).map(
            productResourceDto -> modelMapper.map(productResourceDto, ProductResourceDto.class)
    );
  }

  public Flux<PaSummaryDto> listOnboardedPaByIds(List<String> ids) {
    log.info("listOnboardedPaByIds - ids={}", ids);
    return Flux.fromIterable(ids)
            .flatMap(onboardInstitutionsDao::get)
            .map(OnboardInstitutionEntityToPaSummaryDto::toDto);
  }

}
