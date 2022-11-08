package it.pagopa.pn.external.registries.services;


import java.util.List;
import org.springframework.stereotype.Service;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.mapper.UserGroupToPaGroupDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareInstitutionsClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class InfoSelfcareService {

  // FIXME: da sistemare INSTITUTIONS una volta che le specifiche saranno più chiare
  private static final String MOCK_IMPLEMENTED_MSG =  "richiesta mock implementata";
  private static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

  private final SelfcareUserGroupClient selfcareUserGroupClient;
  private final SelfcareInstitutionsClient selfcareInstitutionsClient;

  public InfoSelfcareService(SelfcareUserGroupClient selfcareUserGroupClient,
      SelfcareInstitutionsClient selfcareInstitutionsClient) {
    this.selfcareUserGroupClient = selfcareUserGroupClient;
    this.selfcareInstitutionsClient = selfcareInstitutionsClient;
  }

  public Mono<PaInfoDto> getOnePa(String id) throws PnRuntimeException {
    log.info("getOnePa - id={}", id);
    /*
     * return selfcareClient.getInstitution(id) .switchIfEmpty(Mono.error(new NotFoundException()))
     * .map(InstitutionResourceDtoToPaInfoDto::toDto);
     */
    return Mono.error(new PnInternalException(MOCK_IMPLEMENTED_MSG, NOT_IMPLEMENTED));
  }

  public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
    log.info("listOnboardedPaByName - paNameFilter={}", paNameFilter);
    /*
     * return selfcareClient.getInstitutions() .filter(inst ->
     * inst.getName().toLowerCase(Locale.ROOT).contains(paNameFilter))
     * .map(InstitutionResourceDtoToPaSummaryDto::toDto);
     */
    return Flux.error(new PnInternalException(MOCK_IMPLEMENTED_MSG, NOT_IMPLEMENTED));
  }

  public Flux<PaSummaryDto> listOnboardedPaByIds(List<String> ids) {
    log.info("listOnboardedPaByIds - ids={}", ids);
    /*
     * return selfcareClient.getInstitutions() .filter(inst -> ids.contains(inst.getId()))
     * .map(InstitutionResourceDtoToPaSummaryDto::toDto);
     */
    return Flux.error(new PnInternalException(MOCK_IMPLEMENTED_MSG, NOT_IMPLEMENTED));
  }

  public Flux<PaGroupDto> getGroups(String xPagopaPnUid, String xPagopaPnCxId,
      List<String> xPagopaPnCxGroups, PaGroupStatusDto statusFilter) {
    log.info("getGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}",
        xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
    return selfcareUserGroupClient.getUserGroups(xPagopaPnCxId)
        .map(PageOfUserGroupResourceDto::getContent).flatMapMany(Flux::fromIterable)
        .filter(grp -> statusFilter == null
            || statusFilter.getValue().equals(grp.getStatus().getValue()))
        .filter(grp -> xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty()
            || xPagopaPnCxGroups.contains(grp.getId()))
        .map(UserGroupToPaGroupDtoMapper::toDto);
  }
}
