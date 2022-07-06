package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.api.MvpContextApi;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.services.MVPValidUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class MVPValidUserController implements MvpContextApi {
    private final MVPValidUserService service;

    public MVPValidUserController(MVPValidUserService service) {
        this.service = service;
    }

    @Override
    public Mono<ResponseEntity<MvpUserDto>> checkValidUsers(Mono<String> body, ServerWebExchange exchange) {
        log.info( "[enter] check valid user" );
        return service.checkValidUser( body )
                .map( b -> {
                    log.debug( "[exit]" );
                    return ResponseEntity.ok( b );
                } )
                .switchIfEmpty( Mono.just( ResponseEntity.<MvpUserDto>notFound().build() ) );
    }
}
