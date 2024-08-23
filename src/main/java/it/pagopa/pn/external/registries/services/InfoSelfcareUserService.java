package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.InstitutionResourcePNDto;
import it.pagopa.pn.external.registries.mapper.InstitutionsToInstitutionPNDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSelfcareUserService {

    private final SelfcarePaInstitutionClient selfcarePaInstitutionClient;

    public Flux<InstitutionResourcePNDto> listUserInstitutionByCurrentUser(String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
        log.info("listInstitutionByCurrentUser - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
        return selfcarePaInstitutionClient.getUserInstitutions(xPagopaPnUid)
                .map(InstitutionsToInstitutionPNDtoMapper::toDto);
    }
}
