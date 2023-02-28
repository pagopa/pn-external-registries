package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.PnCheckoutBadRequestException;
import it.pagopa.pn.external.registries.exceptions.PnCheckoutServerErrorException;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.CartRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.ValidationFaultPaymentProblemJsonDto;
import it.pagopa.pn.external.registries.generated.openapi.delivery.client.v1.dto.PaymentEventPagoPa;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import it.pagopa.pn.external.registries.middleware.msclient.DeliveryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InfoPaymentServiceTest {

    Duration d = Duration.ofMillis(3000);

    private final String CHECKOUT_SITE_URL = "https://uat.checkout.pagopa.it";

    @InjectMocks
    private InfoPaymentService service;

    @Mock
    private SendPaymentNotificationService sendPaymentNotificationService;

    @Mock
    private CheckoutClient checkoutClient;

    @Mock
    private DeliveryClient deliveryClient;

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
        Mockito.when( deliveryClient.paymentEventPagoPaPrivate( Mockito.any( PaymentEventPagoPa.class ) ) ).thenReturn( Mono.empty() );

        PaymentInfoDto result = service.getPaymentInfo( "asdasda", "asdasda" ).block(Duration.ofMillis( 3000 ));

        assertNotNull( result );
        assertEquals( PaymentStatusDto.SUCCEEDED , result.getStatus() );
    }

    @Test
    void getInfoPaymentConflictOnGoing() {

        ValidationFaultPaymentProblemJsonDto responseBody = new ValidationFaultPaymentProblemJsonDto();
        responseBody.setCategory( "PAYMENT_ONGOING" );
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

        PaymentInfoDto result = service.getPaymentInfo( "asdasda", "asdasda" ).block(Duration.ofMillis( 3000 ));

        assertNotNull( result );
        assertEquals( PaymentStatusDto.IN_PROGRESS, result.getStatus() );
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
        PaymentInfoDto result = service.getPaymentInfo( "fake_payment_id", "fakeNoticeNumber" ).block();

        //Then
        assertNotNull( result );
        assertEquals( PaymentStatusDto.REQUIRED , result.getStatus() );
        assertEquals(CHECKOUT_SITE_URL, result.getUrl() );
    }

    @ParameterizedTest
    @ValueSource(ints = { 502, 503, 504})
    void getInfoPaymentKo_500(int statusCode) {
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

        WebClientResponseException ex = WebClientResponseException.Conflict.create(statusCode, "KO", null, responseBodyBites, StandardCharsets.UTF_8);

        Mockito.when(checkoutClient.getPaymentInfo(Mockito.anyString())).thenReturn(Mono.error(ex));

        Mono<PaymentInfoDto> mono = service.getPaymentInfo("asdasda", "asdasda");
        PnCheckoutServerErrorException thrown = assertThrows(
                PnCheckoutServerErrorException.class,
                () -> mono.block(d),
                ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST
        );
        assertTrue(thrown.getMessage().contains("Checkout server error"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 404})
    void getInfoPaymentKo_400(int statusCode) {
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

        WebClientResponseException ex = WebClientResponseException.Conflict.create(statusCode, "KO", null, responseBodyBites, StandardCharsets.UTF_8);

        Mockito.when(checkoutClient.getPaymentInfo(Mockito.anyString())).thenReturn(Mono.error(ex));

        Mono<PaymentInfoDto> mono = service.getPaymentInfo("asdasda", "asdasda");
        PnCheckoutBadRequestException thrown = assertThrows(
                PnCheckoutBadRequestException.class,
                () -> mono.block(d),
                ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND
        );
        assertTrue(thrown.getMessage().contains("Checkout bad request"));
    }

    @Test
    void checkoutCartOk() {
        final String RETURN_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = buildPaymentRequestDto(RETURN_URL);
        CartRequestDto cartRequestDto = service.toCartRequestDto(paymentRequestDto);

        PaymentResponseDto expectedResponse = new PaymentResponseDto().checkoutUrl(paymentRequestDto.getReturnUrl());

        Mockito.when(checkoutClient.checkoutCart(cartRequestDto))
                .thenReturn(Mono.just(ResponseEntity.status(302).header(HttpHeaders.LOCATION, RETURN_URL).build()));

        Mono<PaymentResponseDto> response = service.checkoutCart(paymentRequestDto);

        StepVerifier.create(response)
                .expectSubscription()
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void checkoutCartKoInternalServerError() {
        final String RETURN_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = buildPaymentRequestDto(RETURN_URL);
        CartRequestDto cartRequestDto = service.toCartRequestDto(paymentRequestDto);

        Mockito.when(checkoutClient.checkoutCart(cartRequestDto))
                .thenReturn(Mono.just(ResponseEntity.status(500).build()));

        Mono<PaymentResponseDto> response = service.checkoutCart(paymentRequestDto);

        StepVerifier.create(response)
                .expectSubscription()
                .expectError(PnNotFoundException.class)
                .verify();
    }

    @Test
    void checkoutCartKoBadRequest() {
        final String RETUNR_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = buildPaymentRequestDto(RETUNR_URL);
        CartRequestDto cartRequestDto = service.toCartRequestDto(paymentRequestDto);

        Mockito.when(checkoutClient.checkoutCart(cartRequestDto))
                .thenReturn(Mono.just(ResponseEntity.status(400).build()));

        Mono<PaymentResponseDto> response = service.checkoutCart(paymentRequestDto);

        StepVerifier.create(response)
                .expectSubscription()
                .expectError(PnCheckoutBadRequestException.class)
                .verify();
    }



    private PaymentRequestDto buildPaymentRequestDto(String returnUrl) {
        return new PaymentRequestDto()
                .paymentNotice(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode("77777777777"))
                .returnUrl(returnUrl);
    }

}
