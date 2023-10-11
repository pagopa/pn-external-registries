package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.AooUoIdsApi;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@AllArgsConstructor
public class AooUoIdsController implements AooUoIdsApi {

    private final InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    private static ResponseEntity<Flux<String>> apply(List<String> dto) {
        return ResponseEntity.ok(Flux.fromIterable(dto));
    }

    @Override
    public Mono<ResponseEntity<Flux<String>>> getFilteredAooUoIdPrivate(List<String> id, final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(infoSelfcareInstitutionsService.filterOutRootIds(id)));
    }
}
