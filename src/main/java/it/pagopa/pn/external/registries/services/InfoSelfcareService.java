package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.exceptions.PnException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.mapper.InstitutionResourceDtoToPaInfoDto;
import it.pagopa.pn.external.registries.mapper.InstitutionResourceDtoToPaSummaryDto;
import it.pagopa.pn.external.registries.mapper.UserGroupToPaGroupDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class InfoSelfcareService {

    // FIXME: da sistemare una volta che le specifiche saranno più chiare

    private final SelfcareClient selfcareClient;

    public InfoSelfcareService(SelfcareClient selfcareClient) {
        this.selfcareClient = selfcareClient;
    }

    public Mono<PaInfoDto> getOnePa(String id) throws PnException {
        log.info("getOnePa - id={}", id);
        return selfcareClient.getInstitution(id)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .map(InstitutionResourceDtoToPaInfoDto::toDto);
    }

    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        log.info("listOnboardedPaByName - paNameFilter={}", paNameFilter);
        return selfcareClient.getInstitutions()
                .filter(inst -> inst.getName().toLowerCase(Locale.ROOT).contains(paNameFilter))
                .map(InstitutionResourceDtoToPaSummaryDto::toDto);
    }

    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        log.info("listOnboardedPaByIds - ids={}", ids);
        return selfcareClient.getInstitutions()
                .filter(inst -> ids.contains(inst.getId()))
                .map(InstitutionResourceDtoToPaSummaryDto::toDto);
    }

    public Flux<PaGroupDto> getGroups(String xPagopaPnUid, String xPagopaPnCxId, List<String> xPagopaPnCxGroups) {
        log.info("getGroups - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnCxGroups={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxGroups);
        return selfcareClient.getUserGroups(xPagopaPnCxId)
                .filter(grp -> xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty()
                        || xPagopaPnCxGroups.contains(grp.getId()))
                .map(UserGroupToPaGroupDtoMapper::toDto);
    }

}