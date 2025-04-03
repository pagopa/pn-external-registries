package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.checkout-api-base-url=http://localhost:9999",
        "pn.external-registry.checkout-api-key=fake_api_key",
        "pn.external-registry.checkout-cart-api-base-url=http://localhost:9999"
})
class CheckoutClientTest extends MockAWSObjectsTestConfig {

    @Autowired
    private CheckoutClient client;


    private static ClientAndServer mockServer;


    @BeforeEach
    public void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void getPaymentInfo() {

        PaymentRequestsGetResponseDto responseDto = new PaymentRequestsGetResponseDto()
                .amount( 1200 )
                .description( "f3cf08a09e1b11ec877559d3b4798277" );

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( ValidationFaultPaymentProblemJsonDto.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/payment-requests/77777777777302000100000019421"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        PaymentRequestsGetResponseDto response = client.getPaymentInfo( "77777777777302000100000019421" ).block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals( 1200 , response.getAmount() );
    }

    @Test
    void checkoutCart() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CartRequestDto cartRequestDto = new CartRequestDto()
                .paymentNotices(List.of(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode("77777777777")))
                .returnUrls(new CartRequestReturnUrlsDto()
                        .returnOkUrl(URI.create("https://localhost:433/ok"))
                        .returnErrorUrl(URI.create("https://localhost:433/error"))
                        .returnCancelUrl(URI.create("https://localhost:433/cancel")));

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("POST")
                        .withPath("/carts")
                        .withBody(objectMapper.writeValueAsString(cartRequestDto))
                )
                .respond(response()
                        .withStatusCode(302)
                        .withHeader(HttpHeaders.LOCATION, "https://localhost:433/ok")
                        .withHeader(HttpHeaders.CONNECTION, "keep-alive")
                        .withHeader(HttpHeaders.CONTENT_LENGTH, "0"));

        StepVerifier.create(client.checkoutCart(cartRequestDto))
                .expectSubscription()
                .expectNext(ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, "https://localhost:433/ok")
                        .header(HttpHeaders.CONNECTION, "keep-alive")
                        .header(HttpHeaders.CONTENT_LENGTH, "0")
                        .build())
                .verifyComplete();
    }

    @Test
    void checkoutCartKO422() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CartRequestDto cartRequestDto = new CartRequestDto()
            .paymentNotices(List.of(new PaymentNoticeDto()
                .noticeNumber("302012387654312384")
                .amount(1500)
                .fiscalCode("77777777777")))
            .returnUrls(new CartRequestReturnUrlsDto()
                .returnOkUrl(URI.create("https://localhost:433/ok"))
                .returnErrorUrl(URI.create("https://localhost:433/error"))
                .returnCancelUrl(URI.create("https://localhost:433/cancel")));

        new MockServerClient("localhost", 9999)
            .when(request()
                .withMethod("POST")
                .withPath("/carts")
                .withBody(objectMapper.writeValueAsString(cartRequestDto))
            )
            .respond(response()
                .withStatusCode(422)
                .withHeader(HttpHeaders.LOCATION, "https://localhost:433/ok")
                .withHeader(HttpHeaders.CONNECTION, "keep-alive")
                .withHeader(HttpHeaders.CONTENT_LENGTH, "0"));

        StepVerifier.create(client.checkoutCart(cartRequestDto))
            .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.UnprocessableEntity)
            .verify();
    }

    @Test
    void checkoutCartKO404() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CartRequestDto cartRequestDto = new CartRequestDto()
            .paymentNotices(List.of(new PaymentNoticeDto()
                .noticeNumber("302012387654312384")
                .amount(1500)
                .fiscalCode("77777777777")))
            .returnUrls(new CartRequestReturnUrlsDto()
                .returnOkUrl(URI.create("https://localhost:433/ok"))
                .returnErrorUrl(URI.create("https://localhost:433/error"))
                .returnCancelUrl(URI.create("https://localhost:433/cancel")));

        new MockServerClient("localhost", 9999)
            .when(request()
                .withMethod("POST")
                .withPath("/carts")
                .withBody(objectMapper.writeValueAsString(cartRequestDto))
            )
            .respond(response()
                .withStatusCode(404)
                .withHeader(HttpHeaders.LOCATION, "https://localhost:433/ok")
                .withHeader(HttpHeaders.CONNECTION, "keep-alive")
                .withHeader(HttpHeaders.CONTENT_LENGTH, "0"));

        StepVerifier.create(client.checkoutCart(cartRequestDto))
            .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.NotFound)
            .verify();
    }
}