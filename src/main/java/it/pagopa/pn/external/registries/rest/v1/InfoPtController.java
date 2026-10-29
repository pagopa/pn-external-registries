package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.api.InfoPtApi;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.services.InfoPtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class InfoPtController implements InfoPtApi {

    private final InfoPtService infoPtService;

    @Override
    public Mono<ResponseEntity<PtInstitutionsResponseDto>> getPtInstitutions(String xPagopaPnCxId, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnUid, String xPagopaPnSrcCh, String search, PtDelegationStatusDto status, Integer limit, String lastKey, ServerWebExchange exchange) {
        return Mono.error(new UnsupportedOperationException());
    }
}
