package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDto;
import it.pagopa.pn.external.registries.mapper.UserDataToPgUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgInstitutionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSelfcareUserService {

    private final SelfcarePgInstitutionClient selfcarePgInstitutionClient;

    public Mono<PgUserDto> getPgUserData(String xPagopaPnUid, String xPagopaPnCxId) {
        return selfcarePgInstitutionClient.retrieveUserInstitution(xPagopaPnUid, xPagopaPnCxId)
                .map(UserDataToPgUserDto::toDto);
    }
}
