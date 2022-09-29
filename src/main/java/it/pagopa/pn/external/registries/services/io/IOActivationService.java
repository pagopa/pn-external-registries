package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationPayloadDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.FiscalCodePayloadDto;
import it.pagopa.pn.external.registries.mapper.ActivationToActivationDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class IOActivationService {

    private final IOCourtesyMessageClient client;


    public IOActivationService(IOCourtesyMessageClient client) {
        this.client = client;
    }

    public Mono<ActivationDto> getServiceActivation(Mono<FiscalCodePayloadDto> fiscalCodePayloadDto) {
        return fiscalCodePayloadDto.flatMap(x -> client.getServiceActivation(x.getFiscalCode())
                .map(ActivationToActivationDtoMapper::toDto));
    }

    public Mono<ActivationDto> upsertServiceActivation(Mono<ActivationPayloadDto> activationPayloadDto) {
        return activationPayloadDto.flatMap(x -> client.upsertServiceActivation(x.getFiscalCode(), x.getStatus().equals(ActivationStatusDto.ACTIVE))
                .map(ActivationToActivationDtoMapper::toDto));
    }
}
