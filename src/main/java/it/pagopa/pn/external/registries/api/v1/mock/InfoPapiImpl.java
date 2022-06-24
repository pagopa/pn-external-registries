package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
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

    private final MockResponses mrh;
    private final PnExternalRegistriesConfig config;

    public InfoPapiImpl(MockResponses mrh, PnExternalRegistriesConfig config) {
        this.mrh = mrh;
        this.config = config;
    }

    public Mono<PaInfoDto> getOnePa(String id) throws PnException {
        PaInfoDto paInfo = mrh.getOnePa(id);

        if (paInfo != null) {
            return Mono.just(paInfo);
        } else {
            throw new NotFoundException();
        }
    }

    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        List<PaSummaryDto> list = mrh.listOnboardedPa(paNameFilter);

        return  Flux.fromIterable( list );

    }

    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        List<PaSummaryDto> list = mrh.listOnboardedPa( ids );

        return Flux.fromIterable( list );

    }

}