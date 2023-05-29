package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.PaymentEventPagoPaPrivate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.delivery-base-url=http://localhost:9999"
})
class DeliveryClientTest extends MockAWSObjectsTestConfig {

    @Autowired
    private DeliveryClient deliveryClient;

    @Autowired
    private ObjectMapper objectMapper;


    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void paymentEventPagoPaPrivate() throws JsonProcessingException {

        PaymentEventPagoPaPrivate paymentEventPagoPa = new PaymentEventPagoPaPrivate();
        paymentEventPagoPa.setPaymentDate( "2023-02-27T12:54:12.012Z" );
        paymentEventPagoPa.setUncertainPaymentDate( true );
        paymentEventPagoPa.setAmount( 1200 );
        paymentEventPagoPa.setCreditorTaxId( "77777777777" );
        paymentEventPagoPa.setNoticeCode( "302012387654312384" );

        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withPath( "/delivery-private/events/payment/pagopa" )
                        .withBody( objectMapper.writeValueAsString( paymentEventPagoPa ) ))
                .respond(response()
                        .withStatusCode( 204 )
                );

        StepVerifier.create(deliveryClient.paymentEventPagoPaPrivate(paymentEventPagoPa))
                .expectSubscription()
                .verifyComplete();
    }
}
