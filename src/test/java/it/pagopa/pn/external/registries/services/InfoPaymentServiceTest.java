package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.ValidationFaultPaymentProblemJsonDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@SpringBootTest
class InfoPaymentServiceTest {

    private final String CHECKOUT_BASE_URL = "https://api.uat.platform.pagopa.it/checkout/auth/payments/v2";

    @InjectMocks
    private InfoPaymentService service;

    @Mock
    private CheckoutClient checkoutClient;

    @Mock
    PnExternalRegistriesConfig config;

    @Test
    void getInfoPaymentConflict() {

        ValidationFaultPaymentProblemJsonDto responseBody = new ValidationFaultPaymentProblemJsonDto();
        responseBody.setCategory( "PAYMENT_DUPLICATED" );
        responseBody.detailV2( "PPT_PAGAMENTO_IN_CORSO" );

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( ValidationFaultPaymentProblemJsonDto.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseBody );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        WebClientResponseException ex = WebClientResponseException.Conflict.create( 409, "CONFLICT", null, responseBodyBites , StandardCharsets.UTF_8  );

        Mockito.when( checkoutClient.getPaymentInfo( Mockito.anyString() ) ).thenReturn( Mono.error( ex ) );

        PaymentInfoDto result = service.getPaymentInfo( "asdasda" ).block(Duration.ofMillis( 3000 ));

        Assertions.assertNotNull( result );
        Assertions.assertEquals( PaymentStatusDto.SUCCEEDED , result.getStatus() );
    }

    @Test
    void getInfoPaymentOk() {
        //Given
        PaymentRequestsGetResponseDto paymentResponse = new PaymentRequestsGetResponseDto();
        paymentResponse.setImportoSingoloVersamento( 120 );
        Mono<PaymentRequestsGetResponseDto> checkoutResponse = Mono.just( paymentResponse );

        //When
        Mockito.when( checkoutClient.getPaymentInfo( Mockito.anyString() ) ).thenReturn( checkoutResponse );
        Mockito.when( config.getCheckoutBaseUrl() ).thenReturn( CHECKOUT_BASE_URL );
        PaymentInfoDto result = service.getPaymentInfo( "fake_payment_id" ).block();

        //Then
        Assertions.assertNotNull( result );
        Assertions.assertEquals( PaymentStatusDto.REQUIRED , result.getStatus() );
        Assertions.assertEquals( CHECKOUT_BASE_URL, result.getUrl() );
    }

}