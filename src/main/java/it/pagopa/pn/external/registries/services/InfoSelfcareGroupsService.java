package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupStatusDto;
import it.pagopa.pn.external.registries.mapper.UserGroupToPaGroupDtoMapper;
import it.pagopa.pn.external.registries.mapper.UserGroupToPgGroupDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaUserGroupClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareGroupsService {

    private final SelfcarePaUserGroupClient selfcarePaUserGroupClient;
    private final SelfcarePgUserGroupClient selfcarePgUserGroupClient;

    public InfoSelfcareGroupsService(SelfcarePaUserGroupClient selfcarePaUserGroupClient,
                                     SelfcarePgUserGroupClient selfcarePgUserGroupClient) {
        this.selfcarePaUserGroupClient = selfcarePaUserGroupClient;
        this.selfcarePgUserGroupClient = selfcarePgUserGroupClient;
    }

    public Flux<PaGroupDto> getPaGroups(String xPagopaPnUid,
                                      String xPagopaPnCxId,
                                      List<String> xPagopaPnCxGroups,
                                      PaGroupStatusDto statusFilter) {
        log.info("getGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}",
                xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        return selfcarePaUserGroupClient.getUserGroups(xPagopaPnCxId)
                .map(PageOfUserGroupResourceDto::getContent).flatMapMany(Flux::fromIterable)
                .filter(grp -> statusFilter == null
                        || statusFilter.getValue().equals(grp.getStatus().getValue()))
                .filter(grp -> xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty()
                        || xPagopaPnCxGroups.contains(grp.getId()))
                .map(UserGroupToPaGroupDtoMapper::toDto);
    }

    public Flux<PgGroupDto> getPgGroups(String xPagopaPnUid,
                                        String xPagopaPnCxId,
                                        List<String> xPagopaPnCxGroups,
                                        PgGroupStatusDto statusFilter) {
        log.info("getPgGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}",
                xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        String institutionId = xPagopaPnCxId;
        if (xPagopaPnCxId.startsWith("PG-")) {
            institutionId = xPagopaPnCxId.replaceFirst("PG-", "");
        }
        return selfcarePgUserGroupClient.getUserGroups(institutionId)
                .map(PageOfUserGroupResourceDto::getContent)
                .flatMapMany(Flux::fromIterable)
                .filter(g -> statusFilter == null || statusFilter.getValue().equals(g.getStatus().getValue()))
                .filter(g -> xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty() || xPagopaPnCxGroups.contains(g.getId()))
                .map(UserGroupToPgGroupDtoMapper::toDto);
    }
}
