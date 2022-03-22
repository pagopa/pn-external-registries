package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoPapiImpl {
    public static final String PA_ID = "c_f205";

    public static Mono<ResponseEntity<PaInfoDto>> getOnePa(String id,  final ServerWebExchange exchange) throws PnInternalException {
        PaInfoDto paInfo =  getPaInfo();

        if (PA_ID.equals(id)) {
            return Mono.just(ResponseEntity.ok().body(paInfo));
        } else {
            throw new PnInternalException("Not Found");
        }
    }

    public static Mono<ResponseEntity<Flux<Object>>> listOnboardedPa(String paNameFilter, final ServerWebExchange exchange) {
        PaInfoDto paInfo =  getPaInfo();

        if (paNameFilter == null || PA_ID.startsWith(paNameFilter)) {
            List<PaInfoDto> list = Arrays.asList(paInfo);
            return Mono.just(ResponseEntity.ok().body(Mono.just(list)
                    .flatMapMany(Flux::fromIterable)));

        } else {
            List<PaInfoDto> list = new ArrayList<>();
            return Mono.just(ResponseEntity.ok().body(Mono.just(list)
                    .flatMapMany(Flux::fromIterable)));
        }
    }

    private static PaInfoDto getPaInfo() {
            PaContactsDto pac = new PaContactsDto();
            pac.setEmail("protocollo@comune.milano.it");
            pac.setPec("protocollo@postacert.comune.milano.it");
            pac.setWeb(URI.create("www.comune.milano.it"));
            pac.setTel("0212345678");

            PaInfoDto paInfo = new PaInfoDto();
            paInfo.setId(PA_ID);
            paInfo.setName("Comune di Milano");
            paInfo.setTaxId("01199250158");
            paInfo.setGeneralContacts(pac);

            return paInfo;
    }
}