package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.RootSenderIdApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.RootSenderIdResponseDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@AllArgsConstructor
public class RootSenderIdController implements RootSenderIdApi {

    private final InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    @Override
    public Mono<ResponseEntity<RootSenderIdResponseDto>> getRootSenderIdPrivate(String senderId, ServerWebExchange exchange) {
        return infoSelfcareInstitutionsService.getRootId(senderId).map(ResponseEntity::ok);
    }
}
