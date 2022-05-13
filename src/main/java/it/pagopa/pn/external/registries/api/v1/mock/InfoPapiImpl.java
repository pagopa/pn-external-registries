package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
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
        PaInfoDto paInfo;

        try {
            paInfo = MockResponsees.getMockResp().getOnePa(id);
        } catch (Exception e) {
            log.error("getOnePa error", e);
            throw new InternalErrorException();
        }

        if (paInfo != null) {
            return Mono.just(paInfo);
        } else {
            throw new NotFoundException();
        }
    }

    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        List<PaSummaryDto> list;
        try {
            list = MockResponsees.getMockResp().listOnboardedPa(paNameFilter);
        } catch (Exception e) {
            log.error("listOnboardedPaByName error", e);
            throw new InternalErrorException();
        }

        return  Flux.fromIterable( list );

    }

    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        List<PaSummaryDto> list;
        try {
            list = MockResponsees.getMockResp().listOnboardedPa( ids );
        } catch (Exception e) {
            log.error("listOnboardedPaByIds error", e);
            throw new InternalErrorException();
        }

        return Flux.fromIterable( list );

    }

}