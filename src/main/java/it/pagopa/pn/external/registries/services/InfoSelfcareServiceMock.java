package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.api.v1.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.exceptions.PnException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareServiceMock extends InfoSelfcareService {

    // FIXME: cestinare una volta che InfoSelfcareService è completato e funzionante

    private final InfoPapiImpl infoPapi;

    public InfoSelfcareServiceMock(SelfcareClient selfcareClient, InfoPapiImpl infoPapi) {
        super(selfcareClient);
        this.infoPapi = infoPapi;
    }

    @Override
    public Mono<PaInfoDto> getOnePa(String id) throws PnException {
        return infoPapi.getOnePa(id);
    }

    @Override
    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        return infoPapi.listOnboardedPaByName(paNameFilter);
    }

    @Override
    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        return infoPapi.listOnboardedPaByIds(ids);
    }

}