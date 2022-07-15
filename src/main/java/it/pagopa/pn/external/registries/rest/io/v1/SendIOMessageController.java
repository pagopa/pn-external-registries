package it.pagopa.pn.external.registries.rest.io.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.SendIoMessageApi;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusResponseDto;
import it.pagopa.pn.external.registries.services.io.IOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class SendIOMessageController implements SendIoMessageApi {

    private  final IOService service;

    public SendIOMessageController(IOService service) {
        this.service = service;
    }

    @Override
    public Mono<ResponseEntity<SendMessageResponseDto>> sendIOMessage(Mono<SendMessageRequestDto> sendMessageRequestDto, ServerWebExchange exchange) {
        log.info( "[enter] send IO message" );
        return service.sendIOMessage( sendMessageRequestDto )
                .map( body -> {
                    log.debug( "[exit]" );
                    return ResponseEntity.ok( body );
                } )
                .switchIfEmpty( Mono.just( ResponseEntity.<SendMessageResponseDto>notFound().build() ) );
    }

    public Mono<ResponseEntity<UserStatusResponseDto>> userStatus(Mono<UserStatusRequestDto> userStatusRequestDto, final ServerWebExchange exchange) {
        log.info( "[enter] userStatus" );
        return service.getUserStatus( userStatusRequestDto )
                .map( b -> {
                    log.debug( "[exit]" );
                    return ResponseEntity.ok( b );
                });

    }
}
