package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.AooUoIdsApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.FilteredPaIdsResponseDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
public class AooUoIdsController implements AooUoIdsApi {

    private final InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    @Override
    public Mono<ResponseEntity<FilteredPaIdsResponseDto>> getFilteredAooUoIdV2Private(List<String> id, ServerWebExchange exchange) {
        return infoSelfcareInstitutionsService.filterOutRootIds(id)
                .collectList()
                .map(ids -> ResponseEntity.ok(new FilteredPaIdsResponseDto().ids(ids)));
    }
}
