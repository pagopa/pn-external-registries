package it.pagopa.pn.external.registries.rest.io.v1;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.api.FromIoMessageApi;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.PreconditionContentDto;
import it.pagopa.pn.external.registries.services.io.IOService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class FromIOMessageController implements FromIoMessageApi {

    private final IOService service;

    @Override
    public Mono<ResponseEntity<PreconditionContentDto>> notificationDisclaimer(String xPagopaPnCxId, String iun, final ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        MDC.put(MDCUtils.MDC_CX_ID_KEY, xPagopaPnCxId);
        MDC.put(MDCUtils.MDC_PN_CTX_TOPIC, "notificationDisclaimer");
        return MDCUtils.addMDCToContextAndExecute(service.notificationDisclaimer(xPagopaPnCxId, iun)
                .map(ResponseEntity::ok));
    }
}
