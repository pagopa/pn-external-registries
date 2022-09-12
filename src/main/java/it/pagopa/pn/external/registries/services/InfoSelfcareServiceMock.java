package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.external.registries.api.v1.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareInstitutionsClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class InfoSelfcareServiceMock extends InfoSelfcareService {

    // FIXME: cestinare una volta che InfoSelfcareService è completato e funzionante. Questa classe fa l'override dei metodi di instituions

    private final InfoPapiImpl infoPapi;

    public InfoSelfcareServiceMock(SelfcareUserGroupClient selfcareUserGroupClient, SelfcareInstitutionsClient selfcareInstitutionsClient, InfoPapiImpl infoPapi) {
        super(selfcareUserGroupClient, selfcareInstitutionsClient);
        this.infoPapi = infoPapi;
    }

    @Override
    public Mono<PaInfoDto> getOnePa(String id) throws PnRuntimeException {
        log.info("getOnePa - id={}", id);
        return infoPapi.getOnePa(id);
    }

    @Override
    public Flux<PaSummaryDto> listOnboardedPaByName(String paNameFilter) {
        log.info("listOnboardedPaByName - paNameFilter={}", paNameFilter);
        return infoPapi.listOnboardedPaByName(paNameFilter);
    }

    @Override
    public Flux<PaSummaryDto> listOnboardedPaByIds( List<String> ids) {
        log.info("listOnboardedPaByIds - ids={}", ids);
        return infoPapi.listOnboardedPaByIds(ids);
    }

}