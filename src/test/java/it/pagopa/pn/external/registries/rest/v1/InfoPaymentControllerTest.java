package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.external.registries.services.InfoPaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = {InfoPaymentController.class})
class InfoPaymentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    InfoPaymentService service;

    @Test
    void getPaymentInfoSuccess() {
        //Given
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setStatus( PaymentStatusDto.SUCCEEDED );
        dto.setAmount( 20 );

        String url = "/ext-registry/pa/v1/paymentinfo/{paymentId}"
                .replace( "{paymentId}", "77777777777302000100000019421" );

        //When
        Mockito.when( service.getPaymentInfo( Mockito.anyString() ) ).thenReturn( Mono.just( dto ) );

        //Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk();

    }

}