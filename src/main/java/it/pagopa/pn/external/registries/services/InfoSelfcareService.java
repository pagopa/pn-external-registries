package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import it.pagopa.pn.external.registries.exceptions.PnException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.mapper.UserGroupToPaGroupDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareInstitutionsClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareService {

    // FIXME: da sistemare INSTITUTIONS una volta che le specifiche saranno più chiare

    private final SelfcareUserGroupClient selfcareUserGroupClient;
    private final SelfcareInstitutionsClient selfcareInstitutionsClient;

    public InfoSelfcareService(SelfcareUserGroupClient selfcareUserGroupClient, SelfcareInstitutionsClient selfcareInstitutionsClient) {
        this.selfcareUserGroupClient = selfcareUserGroupClient;
        this.selfcareInstitutionsClient = selfcareInstitutionsClient;
    }

    public Mono<PaInfoDto> getOnePa(String id) throws PnException {
        log.info("getOnePa - id={}", id);
        /*return selfcareClient.getInstitution(id)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .map(InstitutionResourceDtoToPaInfoDto::toDto);*/
        return Mono.error(new InternalErrorException());
    }

    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        log.info("listOnboardedPaByName - paNameFilter={}", paNameFilter);
        /*return selfcareClient.getInstitutions()
                .filter(inst -> inst.getName().toLowerCase(Locale.ROOT).contains(paNameFilter))
                .map(InstitutionResourceDtoToPaSummaryDto::toDto);*/
        return Flux.error(new InternalErrorException());
    }

    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        log.info("listOnboardedPaByIds - ids={}", ids);
        /*return selfcareClient.getInstitutions()
                .filter(inst -> ids.contains(inst.getId()))
                .map(InstitutionResourceDtoToPaSummaryDto::toDto);*/
        return Flux.error(new InternalErrorException());
    }

    public Flux<PaGroupDto> getGroups(String xPagopaPnUid, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, PaGroupStatusDto statusFilter) {
        log.info("getGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={} statusFilter={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups, statusFilter);
        return selfcareUserGroupClient.getUserGroups(xPagopaPnCxId)
                .filter(grp -> statusFilter == null || statusFilter.getValue().equals(grp.getStatus().getValue()))
                .filter(grp -> xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty()
                        || xPagopaPnCxGroups.contains(grp.getId()))
                .map(UserGroupToPaGroupDtoMapper::toDto);
    }

}