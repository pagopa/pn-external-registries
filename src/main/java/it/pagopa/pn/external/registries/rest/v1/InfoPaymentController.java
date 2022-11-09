package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.PaymentInfoApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.services.InfoPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class InfoPaymentController implements PaymentInfoApi {

    private final InfoPaymentService infoPaymentService;

    public InfoPaymentController(InfoPaymentService infoPaymentService) {
        this.infoPaymentService = infoPaymentService;
    }

    @Override
    public Mono<ResponseEntity<PaymentInfoDto>> getPaymentInfo(String paTaxId, String noticeNumber, ServerWebExchange exchange) {
        log.info("[enter] paTaxId:{} ,noticeNumber:{}", paTaxId, noticeNumber);
        return this.infoPaymentService.getPaymentInfo( paTaxId , noticeNumber )
                .map(body -> {
                    log.debug("[exit]");
                    return ResponseEntity.ok(body);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.<Flux<PaymentInfoDto>>notFound().build()));
    }
    
}
