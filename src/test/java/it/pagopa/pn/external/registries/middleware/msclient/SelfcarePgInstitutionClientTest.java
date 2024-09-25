package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserResponseDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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


    @BeforeEach
    public void startMockServer() {
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

    @Test
    void retrieveUserInstitutionTestFailsWhenDoesntFindData() {
        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/users"))
                    .respond(response()
                            .withBody("[]".getBytes(StandardCharsets.UTF_8))
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withStatusCode(200));

            assertThrows(PnInternalException.class, () -> client.retrieveUserInstitution("uid", "cxId").block());
        }
    }

    @Test
    void retrieveUserDetailReturnsUserResponse() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId("uid");
        userResponseDto.setTaxCode("taxCode");

        byte[] responseBytes = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        try {
            responseBytes = mapper.writeValueAsBytes(userResponseDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/users/"+userResponseDto.getId()))
                    .respond(response()
                            .withBody(responseBytes)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withStatusCode(200));
            // When
            UserResponseDto response = client.retrieveUserDetail("uid", "cxId").block();

            // Then
            assertNotNull(response);
        }
    }

    @Test
    void retrieveUserDetailFailsWhenDoesntFindData() {
        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/user-info"))
                    .respond(response()
                            .withBody("[]".getBytes(StandardCharsets.UTF_8))
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withStatusCode(200));

            assertThrows(PnInternalException.class, () -> client.retrieveUserDetail("uid", "cxId").block());
        }
    }

    @Test
    void retrieveUserDetailHandlesWebClientResponseException() {
        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/user-info"))
                    .respond(response()
                            .withStatusCode(500));

            assertThrows(PnInternalException.class, () -> client.retrieveUserDetail("uid", "cxId").block());
        }
    }
}