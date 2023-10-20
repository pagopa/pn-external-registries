package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.PnCheckoutBadRequestException;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.CartRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.ValidationFaultPaymentProblemJsonDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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


    @Test
    void getInfoPaymentConflict() {

        ValidationFaultPaymentProblemJsonDto responseBody = new ValidationFaultPaymentProblemJsonDto();
        responseBody.setCategory( "PAYMENT_DUPLICATED" );
        responseBody.detailV2( "PPT_PAGAMENTO_IN_CORSO" );

        byte[] responseBodyBytes = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( ValidationFaultPaymentProblemJsonDto.class );
        try {
            responseBodyBytes = mapper.writeValueAsBytes( responseBody );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        WebClientResponseException ex = WebClientResponseException.Conflict.create( 409, "CONFLICT", null, responseBodyBytes , StandardCharsets.UTF_8  );

        Mockito.when( checkoutClient.getPaymentInfo( Mockito.anyString() ) ).thenReturn( Mono.error( ex ) );

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("asdasda");
        requestInnerDto.setNoticeCode("asdasda");

        List<PaymentInfoV21Dto> result = service.getPaymentInfo(Flux.just(requestInnerDto)).block(Duration.ofMillis( 3000 ));

        assertNotNull( result );
        assertEquals( PaymentStatusDto.SUCCEEDED , result.get(0).getStatus() );
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

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("asdasda");
        requestInnerDto.setNoticeCode("asdasda");

        List<PaymentInfoV21Dto> result = service.getPaymentInfo( Flux.just(requestInnerDto) ).block(Duration.ofMillis( 3000 ));

        assertNotNull( result );
        assertEquals( PaymentStatusDto.IN_PROGRESS, result.get(0).getStatus() );
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

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("fake_payment_id");
        requestInnerDto.setNoticeCode("fakeNoticeNumber");
        List<PaymentInfoV21Dto> result = service.getPaymentInfo( Flux.just(requestInnerDto) ).block();

        //Then
        assertNotNull( result );
        assertEquals( PaymentStatusDto.REQUIRED , result.get(0).getStatus() );
        assertEquals(CHECKOUT_SITE_URL, result.get(0).getUrl() );
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

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("asdasda");
        requestInnerDto.setNoticeCode("asdasda");

        List<PaymentInfoV21Dto> result = service.getPaymentInfo( Flux.just(requestInnerDto) ).block();

        //Then
        assertNotNull( result );
        assertEquals( PaymentStatusDto.FAILURE, result.get(0).getStatus() );
        assertEquals( DetailDto.GENERIC_ERROR, result.get(0).getDetail());
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

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("asdasda");
        requestInnerDto.setNoticeCode("asdasda");

        List<PaymentInfoV21Dto> result = service.getPaymentInfo( Flux.just(requestInnerDto) ).block();

        //Then
        assertNotNull( result );
        assertEquals( PaymentStatusDto.FAILURE, result.get(0).getStatus() );
        assertEquals( DetailDto.GENERIC_ERROR, result.get(0).getDetail());
    }
    @Test
    void getInfoPaymentKo_JsonProcessingException() {

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(String.class);
        try {
            responseBodyBites = mapper.writeValueAsBytes("test");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        WebClientResponseException ex = WebClientResponseException.Conflict.create(HttpStatus.INTERNAL_SERVER_ERROR.value(), "KO", null, responseBodyBites, StandardCharsets.UTF_8);

        Mockito.when(checkoutClient.getPaymentInfo(Mockito.anyString())).thenReturn(Mono.error(ex));

        PaymentInfoRequestDto requestInnerDto = new PaymentInfoRequestDto();
        requestInnerDto.setCreditorTaxId("asdasda");
        requestInnerDto.setNoticeCode("asdasda");

        List<PaymentInfoV21Dto> result = service.getPaymentInfo( Flux.just(requestInnerDto) ).block();

        //Then
        assertNotNull( result );
        assertEquals( PaymentStatusDto.FAILURE, result.get(0).getStatus() );
        assertEquals( DetailDto.GENERIC_ERROR, result.get(0).getDetail());
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
