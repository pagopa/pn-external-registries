package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.EnteBeneficiarioDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.ValidationFaultPaymentProblemJsonDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.UserGroupPlainResourceDto;
import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcare-base-url=http://localhost:9999"
})
class SelfcareClientTest {

    @Autowired
    private SelfcareClient client;

    @MockBean
    private AssertionGenerator assertionGenerator;

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
    void getInstitution() {

        InstitutionResourceDto responseDto = new InstitutionResourceDto()
                .name("companyname");

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( InstitutionResourceDto.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/institutions/{institutionId}".replace("{institutionId}", "77777777777302000100000019421")))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        InstitutionResourceDto response = client.getInstitution( "77777777777302000100000019421" ).block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals( responseDto.getName() , response.getName() );
    }

    @Test
    void getInstitutions() {

        InstitutionResourceDto responseDto = new InstitutionResourceDto()
                .name("companyname");

        byte[] responseBodyBites = new byte[0];



        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( new TypeReference<List<InstitutionResourceDto>>(){});
        try {
            responseBodyBites = mapper.writeValueAsBytes( List.of(responseDto) );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/institutions"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<InstitutionResourceDto> response = client.getInstitutions().collectList().block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals( responseDto.getName() , response.get(0).getName() );
    }

    @Test
    void getUserGroups() {

        UserGroupPlainResourceDto responseDto = new UserGroupPlainResourceDto()
                .name("companyname");

        byte[] responseBodyBites = new byte[0];


        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( new TypeReference<List<UserGroupPlainResourceDto>>(){});
        try {
            responseBodyBites = mapper.writeValueAsBytes( List.of(responseDto) );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/user-groups"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<UserGroupPlainResourceDto> response = client.getUserGroups("77777777777302000100000019421").collectList().block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals( responseDto.getName() , response.get(0).getName() );
    }
}