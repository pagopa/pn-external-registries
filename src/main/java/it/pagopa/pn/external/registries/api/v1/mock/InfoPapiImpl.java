package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class InfoPapiImpl {
    public static Mono<ResponseEntity<PaInfoDto>> getOnePa(String id,  final ServerWebExchange exchange) throws PnInternalException {
        PaInfoDto paInfo = null;

        try {
            paInfo = MockResponsees.getMockResp().getOnePa(id);
        } catch (Exception e) {
            throw new PnInternalException("invalid mock file: " + MockResponsees.mockFile);
        }

        if (paInfo != null) {
            return Mono.just(ResponseEntity.ok().body(paInfo));
        } else {
            throw new PnInternalException("Not Found");
        }
    }

    public static Mono<ResponseEntity<Flux<PaSummaryDto>>> listOnboardedPa(String paNameFilter, final ServerWebExchange exchange) {
        List<PaSummaryDto> list = null;
        try {
            list = MockResponsees.getMockResp().listOnboardedPa(paNameFilter);
        } catch (Exception e) {
            throw new PnInternalException("invalid mock file: " + MockResponsees.mockFile);
        }

        return Mono.just(ResponseEntity.ok().body(Mono.just(list)
                    .flatMapMany(Flux::fromIterable)));

    }

}