package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.PaymentInfoApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
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
    public Mono<ResponseEntity<Flux<PaymentInfoV21InnerDto>>> getPaymentInfoV21(Flux<PaymentInfoRequestDto> paymentInfoRequestDto, ServerWebExchange exchange) {
        return infoPaymentService.getPaymentInfo(paymentInfoRequestDto)
                .map(body -> {
                    log.debug("[exit]");
                    return ResponseEntity.ok(Flux.fromIterable(body));
                });
    }

    @Override
    public Mono<ResponseEntity<PaymentResponseDto>> checkoutCart(Mono<PaymentRequestDto> paymentRequestDto, final ServerWebExchange exchange) {
        return paymentRequestDto.flatMap(infoPaymentService::checkoutCart)
                .map(ResponseEntity::ok);
    }
    
}
