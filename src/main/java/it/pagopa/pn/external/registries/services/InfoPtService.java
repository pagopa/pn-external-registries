package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.middleware.db.dao.PtDelegationsDao;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@CustomLog
@Service
@AllArgsConstructor
public class InfoPtService {

    private final PtDelegationsDao ptDelegationsDao;

    public Mono<PtInstitutionsResponseDto> getPtInstitutions(String ptId, String search, PtDelegationStatusDto status, int limit, String lastKey) {
        return Mono.error(new UnsupportedOperationException());
    }
}
