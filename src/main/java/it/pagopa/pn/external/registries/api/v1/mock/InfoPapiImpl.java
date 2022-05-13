package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.exceptions.PnException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class InfoPapiImpl {
    public Mono<PaInfoDto> getOnePa(String id) throws PnException {
        PaInfoDto paInfo = MockResponsees.getMockResp().getOnePa(id);

        if (paInfo != null) {
            return Mono.just(paInfo);
        } else {
            throw new NotFoundException();
        }
    }

    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        List<PaSummaryDto> list = MockResponsees.getMockResp().listOnboardedPa(paNameFilter);

        return  Flux.fromIterable( list );

    }

    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        List<PaSummaryDto> list = MockResponsees.getMockResp().listOnboardedPa( ids );

        return Flux.fromIterable( list );

    }

}