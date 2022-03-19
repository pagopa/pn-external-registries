package it.pagopa.pn.external.registries.rest;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.InfoPaApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class InfoPaController implements InfoPaApi {
    @Override
    public Mono<ResponseEntity<PaInfoDto>> getOnePa(String id, ServerWebExchange exchange) {
        return InfoPaApi.super.getOnePa(id, exchange);
    }

    @Override
    public Mono<ResponseEntity<Flux<Object>>> listOnboardedPa(String paNameFilter, ServerWebExchange exchange) {
        return InfoPaApi.super.listOnboardedPa(paNameFilter, exchange);
    }
}
