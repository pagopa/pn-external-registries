package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcarepgusergroup-base-url=http://localhost:9999"
})
class SelfcarePgInstitutionClientTest extends MockAWSObjectsTestConfig {


    private static ClientAndServer mockServer;

    @Autowired
    private SelfcarePgInstitutionClient client;


    @BeforeAll
    public static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void retrieveUserInstitutionTest() {
        UserInstitutionResourceDto userDto = new UserInstitutionResourceDto();
        userDto.setInstitutionId("cxId");
        userDto.setUserId("uid");

        byte[] responseBytes = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        try {
            responseBytes = mapper.writeValueAsBytes(userDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/users"))
                    .respond(response()
                            .withBody(responseBytes)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withStatusCode(200));
            // When
            UserInstitutionResourceDto response = client.retrieveUserInstitution("uid", "cxId").block();

            // Then
            assertNotNull(response);
        }
    }
}