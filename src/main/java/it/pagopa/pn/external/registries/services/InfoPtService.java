package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.mapper.PtDelegationEntityToPtInstitutionDtoMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.PtDelegationsDao;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@CustomLog
@Service
@AllArgsConstructor
public class InfoPtService {

    private final PtDelegationsDao ptDelegationsDao;

    public Mono<PtInstitutionsResponseDto> getPtInstitutions(String ptId, String search, PtDelegationStatusDto status, int limit, String lastKey) {
        log.info("start retrieving institutions for PT: [{}] search: [{}] status: [{}] limit: [{}] lastKey: [{}]", ptId, search, status, limit, lastKey);
        String normalizedSearch = normalizeSearch(search);
        return ptDelegationsDao.getLatestEventPerInstitution(ptId, lastKey)
                .map(PtDelegationEntityToPtInstitutionDtoMapper::toDto)
                .filter(institution -> status == null || status == institution.getStatus())
                .filter(institution -> matchesSearch(institution, normalizedSearch))
                .take(limit + 1L)
                .collectList()
                .map(institutions -> toResponse(institutions, limit));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesSearch(PtInstitutionDto institution, String search) {
        if (search == null) {
            return true;
        }
        return contains(institution.getInstitutionName(), search) || contains(institution.getInstitutionTaxCode(), search);
    }

    private boolean contains(String field, String value) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(value);
    }

    private PtInstitutionsResponseDto toResponse(List<PtInstitutionDto> institutions, int limit) {
        PtInstitutionsResponseDto response = new PtInstitutionsResponseDto();
        if (institutions.size() > limit) {
            List<PtInstitutionDto> page = institutions.subList(0, limit);
            response.setItems(page);
            response.setLastKey(page.get(limit - 1).getInstitutionId());
        } else {
            response.setItems(institutions);
        }
        return response;
    }
}
