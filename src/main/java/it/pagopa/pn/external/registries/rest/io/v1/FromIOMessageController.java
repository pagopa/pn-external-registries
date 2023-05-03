package it.pagopa.pn.external.registries.rest.io.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.FromIoMessageApi;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.PreconditionContentDto;
import it.pagopa.pn.external.registries.services.io.IOService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class FromIOMessageController implements FromIoMessageApi {

    private  final IOService service;

    @Override
    public Mono<ResponseEntity<PreconditionContentDto>> notificationDisclaimer(String xPagopaPnUid, String iun, final ServerWebExchange exchange) {
        return service.notificationDisclaimer(xPagopaPnUid, iun)
                .map(ResponseEntity::ok);
    }
}
