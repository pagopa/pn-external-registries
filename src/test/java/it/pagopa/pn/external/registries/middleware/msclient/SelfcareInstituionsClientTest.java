package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcare-base-url=http://localhost:9999"
})
@Disabled()
class SelfcareInstituionsClientTest {

    @Autowired
    private SelfcareInstitutionsClient client;

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
/*
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
 */
}