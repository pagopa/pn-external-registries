package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.ValidationFaultPaymentProblemJsonDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@SpringBootTest
class InfoPaymentServiceTest {

    private final String CHECKOUT_SITE_URL = "https://uat.checkout.pagopa.it";

    @InjectMocks
    private InfoPaymentService service;

    @Mock
    private SendPaymentNotificationService sendPaymentNotificationService;

    @Mock
    private CheckoutClient checkoutClient;

    @Mock
    PnExternalRegistriesConfig config;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public SqsNotificationPaidProducer sqsNotificationPaidProducer() {
            return Mockito.mock( SqsNotificationPaidProducer.class);
        }
    }

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
        Mockito.when( sendPaymentNotificationService.sendPaymentNotification( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( Mono.empty() );

        PaymentInfoDto result = service.getPaymentInfo( "asdasda", "asdasda" ).block(Duration.ofMillis( 3000 ));

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
        Mockito.when( config.getCheckoutSiteUrl() ).thenReturn(CHECKOUT_SITE_URL);
        Mockito.when( sendPaymentNotificationService.sendPaymentNotification( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( Mono.empty() );
        PaymentInfoDto result = service.getPaymentInfo( "fake_payment_id", "fakeNoticeNumber" ).block();

        //Then
        Assertions.assertNotNull( result );
        Assertions.assertEquals( PaymentStatusDto.REQUIRED , result.getStatus() );
        Assertions.assertEquals(CHECKOUT_SITE_URL, result.getUrl() );
    }

    @Test
    void getInfoPaymentKo() {
        ValidationFaultPaymentProblemJsonDto responseBody = new ValidationFaultPaymentProblemJsonDto();
        responseBody.setCategory("GENERIC_ERROR");

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(ValidationFaultPaymentProblemJsonDto.class);
        try {
            responseBodyBites = mapper.writeValueAsBytes(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        WebClientResponseException ex = WebClientResponseException.Conflict.create(502, "KO", null, responseBodyBites, StandardCharsets.UTF_8);

        Mockito.when(checkoutClient.getPaymentInfo(Mockito.anyString())).thenReturn(Mono.error(ex));
        Mockito.when(sendPaymentNotificationService.sendPaymentNotification(Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.empty());

        PaymentInfoDto result = service.getPaymentInfo("asdasda", "asdasda").block(Duration.ofMillis(3000));

        Assertions.assertNotNull(result);
        System.out.println(result);
        Assertions.assertEquals(PaymentStatusDto.FAILURE, result.getStatus());
    }

}
