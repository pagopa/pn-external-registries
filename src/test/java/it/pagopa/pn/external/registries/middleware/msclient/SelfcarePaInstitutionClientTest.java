package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcareusergroup-base-url=http://localhost:9999"
})
class SelfcarePaInstitutionClientTest extends MockAWSObjectsTestConfig {

    @Autowired
    private SelfcarePaInstitutionClient client;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockServer() { mockServer = startClientAndServer(9999); }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }




    @Test
    void getInstitutionProducts() {
        ProductResourceDto productResourceDto = new ProductResourceDto();
        productResourceDto.setId("test-pn");
        String institutionId = UUID.randomUUID().toString();
        List<ProductResourceDto> list = new ArrayList<>();
        list.add(productResourceDto);

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        try {
            responseBodyBites = mapper.writeValueAsBytes(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                            .withMethod("GET")
                            .withPath("/institutions/" + institutionId + "/products"))
                    .respond(response()
                            .withBody(responseBodyBites)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withStatusCode(200));

            //When
            List<ProductResourceDto> response = client.getInstitutionProducts(institutionId, "1a2qp213-f1cb-4021-b3d0-5241216a0622").collectList().block();

            //Then
            Assertions.assertNotNull(response);
            Assertions.assertEquals(1, response.size());
            Assertions.assertEquals(response.get(0).getId(), productResourceDto.getId());
    }

    @Test
    void getInstitutionProductsKO() {
        String institutionId = UUID.randomUUID().toString();
        int status = 200;

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(new TypeReference<List<ProductResourceDto>>(){});

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/institutions/" + institutionId + "/products"))
                .respond(response().withStatusCode(500));

        //When
        try {
            client.getInstitutionProducts(institutionId, "1a2qp213-f1cb-4021-b3d0-5241216a0633").collectList().block();
        }
        catch (Exception e) {
            status = ((PnInternalException) e).getStatus();
        }

        //Then
        Assertions.assertEquals(500, status);
    }

    @Test
    void getInstitutionsKO() {
        int status = 200;
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(new TypeReference<List<InstitutionResourceDto>>(){});

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/institutions"))
                .respond(response().withStatusCode(500));

        //When
        try {
            client.getInstitutions("1a2qp213-f1cb-4021-b3d0-5241216a0633").collectList().block();
        }
        catch (Exception e) {
            status = ((PnInternalException) e).getStatus();
        }

        //Then
        Assertions.assertEquals(500, status);
    }

    @Test
    void getInstitutions() {
        InstitutionResourceDto institutionResourceDto = new InstitutionResourceDto();
        institutionResourceDto.setAddress("Via vittorio veneto, 23");

        List<InstitutionResourceDto> list = new ArrayList<>();
        list.add(institutionResourceDto);

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(new TypeReference<List<InstitutionResourceDto>>(){});
        try {
            responseBodyBites = mapper.writeValueAsBytes(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/institutions"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<InstitutionResourceDto> response = client.getInstitutions("1a2qp213-f1cb-4021-b3d0-5241216a0622").collectList().block();

        //Then
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(response.get(0).getAddress(), institutionResourceDto.getAddress());
    }

    @Test
    void getUserInstitutionKO() {
        int status = 200;
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(new TypeReference<List<InstitutionResourceDto>>(){});

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/users"))
                .respond(response().withStatusCode(500));

        //When
        try {
            client.getInstitutions("1a2qp213-f1cb-4021-b3d0-5241216a0633").collectList().block();
        }
        catch (Exception e) {
            status = ((PnInternalException) e).getStatus();
        }

        //Then
        Assertions.assertEquals(500, status);
    }

    @Test
    void getUserInstitutionOk() {
        UserInstitutionResourceDto resourceDto = new UserInstitutionResourceDto();
        resourceDto.setInstitutionId("id");
        resourceDto.setUserId("1a2qp213-f1cb-4021-b3d0-5241216a0622");

        List<UserInstitutionResourceDto> list = new ArrayList<>();
        list.add(resourceDto);

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor(new TypeReference<List<InstitutionResourceDto>>(){});
        try {
            responseBodyBites = mapper.writeValueAsBytes(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient.reset();
        mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/users"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        StepVerifier.create(client.getUserInstitutions("1a2qp213-f1cb-4021-b3d0-5241216a0622"))
                .expectNextCount(1)
                        .verifyComplete();
    }
}
