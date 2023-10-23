package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.deliverypush.v1.dto.ProbableSchedulingAnalogDateResponse;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.gpd-api-base-url=http://localhost:9999",
        "pn.external-registry.gpd-api-key=fakeApiKey",
})
class GpdClientTest extends MockAWSObjectsTestConfig {
    @Autowired
    private GpdClient gpdClient;

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
    void getPaymentInfo() throws JsonProcessingException {
        String creditorTaxId="77777777777";
        String noticeCode="347000000000000044";
        String requestId= "test";
        Long notificationFee = 100L;
        
        String iuv = noticeCode.substring(1);

        var expectedBodyResponse = new PaymentsModelResponse()
                .amount(100L);

        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient
                    .when(request()
                            .withMethod("PUT")
                            .withPath("/organizations/" + creditorTaxId + "/paymentoptions/" + iuv + "/notificationfee")
                    )
                    .respond(response()
                            .withStatusCode(200)
                            .withBody(objectMapper.writeValueAsString(expectedBodyResponse), MediaType.APPLICATION_JSON)
                    );

            ResponseEntity<PaymentsModelResponse> res = gpdClient.setNotificationCost(creditorTaxId,noticeCode,requestId, notificationFee).block();

            Assertions.assertNotNull(res);
            PaymentsModelResponse responseReceived = res.getBody();
            Assertions.assertEquals(expectedBodyResponse, responseReceived);
        }
    }
}