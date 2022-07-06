package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.ApiUtil;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.IoActivationApi;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.SendIoMessageApi;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.services.IOActivationService;
import it.pagopa.pn.external.registries.services.SendIOMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class IOActivationController implements IoActivationApi {

    private  final IOActivationService service;

    public IOActivationController(IOActivationService service) {
        this.service = service;
    }

    @Override
    public Mono<ResponseEntity<ActivationDto>> getServiceActivationByPOST(Mono<FiscalCodePayloadDto> fiscalCodePayloadDto, final ServerWebExchange exchange) {
        log.info( "[enter] getServiceActivationByPOST");
        return service.getServiceActivation( fiscalCodePayloadDto )
                .map( body -> {
                    log.debug( "[exit] getServiceActivationByPOST");
                    return ResponseEntity.ok( body );
                } )
                .switchIfEmpty( Mono.error( new NotFoundException()) );

    }

    @Override
    public Mono<ResponseEntity<ActivationDto>> upsertServiceActivation(Mono<ActivationPayloadDto> activationPayloadDto, final ServerWebExchange exchange) {
        log.info( "[enter] upsertServiceActivation");
        return service.upsertServiceActivation( activationPayloadDto )
                .map( body -> {
                    log.debug( "[exit] upsertServiceActivation");
                    return ResponseEntity.ok( body );
                } )
                .switchIfEmpty( Mono.error( new NotFoundException()) );
    }
}
