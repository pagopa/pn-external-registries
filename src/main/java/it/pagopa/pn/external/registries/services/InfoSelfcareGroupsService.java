package it.pagopa.pn.external.registries.services;


import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.mapper.UserGroupToPaGroupDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareGroupsService {


  private final SelfcareUserGroupClient selfcareUserGroupClient;

  public InfoSelfcareGroupsService(SelfcareUserGroupClient selfcareUserGroupClient) {
    this.selfcareUserGroupClient = selfcareUserGroupClient;
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
