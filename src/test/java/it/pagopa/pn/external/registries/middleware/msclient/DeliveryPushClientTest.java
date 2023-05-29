package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.deliverypush.v1.dto.ProbableSchedulingAnalogDateResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
        "pn.external-registry.delivery-push-base-url=http://localhost:9999"
})
class DeliveryPushClientTest extends MockAWSObjectsTestConfig {

    @Autowired
    private DeliveryPushClient client;

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
    void getSchedulingAnalogDateWithHttpInfoTest() throws JsonProcessingException {
        String iun = "a-iun";
        String recipientId = "a-recipientId";

        var expectedBodyResponse = new ProbableSchedulingAnalogDateResponse()
                .iun(iun)
                .recIndex(0)
                .schedulingAnalogDate(Instant.now());

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/delivery-push-private/scheduling-analog-date/" + iun + "/" + recipientId)
                )
                .respond(response()
                        .withStatusCode(200)
                        .withBody(objectMapper.writeValueAsString(expectedBodyResponse), MediaType.APPLICATION_JSON)
                );

        ResponseEntity<ProbableSchedulingAnalogDateResponse> response = client.getSchedulingAnalogDateWithHttpInfo(iun, recipientId).block();
        System.out.println(response);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedBodyResponse);

    }
}
