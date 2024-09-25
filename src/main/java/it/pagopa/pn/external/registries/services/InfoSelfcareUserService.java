package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.InstitutionResourcePNDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDetailDto;
import it.pagopa.pn.external.registries.mapper.InstitutionsToInstitutionPNDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDto;
import it.pagopa.pn.external.registries.mapper.UserDataToPgUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgInstitutionClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSelfcareUserService {

    private final SelfcarePaInstitutionClient selfcarePaInstitutionClient;
    private final SelfcarePgInstitutionClient selfcarePgInstitutionClient;

    public Flux<InstitutionResourcePNDto> listUserInstitutionByCurrentUser(String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnSrcCh, List<String> xPagopaPnCxGroups, String xPagopaPnSrcChDetails) {
        log.info("listInstitutionByCurrentUser - xPagopaPnUid={} xPagopaPnCxId={} xPagopaPnSrcCh={} xPagopaPnCxGroups={} xPagopaPnSrcChDetails={}", xPagopaPnUid, xPagopaPnCxId, xPagopaPnSrcCh, xPagopaPnCxGroups, xPagopaPnSrcChDetails);
        return selfcarePaInstitutionClient.getUserInstitutions(xPagopaPnUid)
                .map(InstitutionsToInstitutionPNDtoMapper::toDto);
    }

    public Mono<PgUserDto> getPgUserData(String xPagopaPnUid, String xPagopaPnCxId) {
        return selfcarePgInstitutionClient.retrieveUserInstitution(xPagopaPnUid, xPagopaPnCxId)
                .map(UserDataToPgUserDto::toDto);
    }

    public Mono<PgUserDetailDto> getPgUserDetails(String xPagopaPnUid, String xPagopaPnCxId) {
        return selfcarePgInstitutionClient.retrieveUserDetail(xPagopaPnUid, xPagopaPnCxId)
                .map(UserDataToPgUserDto::toDto);
    }
}
