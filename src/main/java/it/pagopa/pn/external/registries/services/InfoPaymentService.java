package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class InfoPaymentService {
    private final CheckoutClient checkoutClient;

    public InfoPaymentService(CheckoutClient checkoutClient) {
        this.checkoutClient = checkoutClient;
    }

    public Mono<PaymentInfoDto> getPaymentInfo(String paymentId) {
        return checkoutClient.getPaymentInfo( paymentId )
                .map( r -> {
                    Mono<PaymentInfoDto> result = fromCheckoutToPn(r);
                    return result;
                }).block();
    }

    private Mono<PaymentInfoDto> fromCheckoutToPn(PaymentRequestsGetResponseDto checkoutResult) {
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        paymentInfoDto.setAmount( checkoutResult.getImportoSingoloVersamento() );
        paymentInfoDto.setStatus( PaymentStatusDto.SUCCEEDED );
        return Mono.just( paymentInfoDto );
    }
}
