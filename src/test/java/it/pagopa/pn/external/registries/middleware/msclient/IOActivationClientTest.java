package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.io-base-url=http://localhost:9999",
        "pn.external-registry.io-api-key=fake_api_key"
})
class IOActivationClientTest {

    @Autowired
    private IOActivationClient client;

    private ClientAndServer mockServer;

    @BeforeEach
    public void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void upsertServiceActivation() {
        //Given
        Activation responseDto = new Activation();
        responseDto.setFiscalCode("EEEEEE00E00E000A");
        responseDto.setStatus("ACTIVE");
        responseDto.setVersion(1);
        responseDto.setServiceId("PN");

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( Activation.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        ActivationPayload fiscalCodePayload = new ActivationPayload();
        fiscalCodePayload.setFiscalCode( "EEEEEE00E00E000A" );
        fiscalCodePayload.setStatus("ACTIVE");
        byte[] reqBodyBites = new byte[0];

        mapper.writerFor( ActivationPayload.class );
        try {
            reqBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "PUT" )
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/activations/" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        Activation limitedProfile = client.upsertServiceActivation( fiscalCodePayload.getFiscalCode(), true ).block();

        //Then
        Assertions.assertEquals( "ACTIVE", limitedProfile.getStatus() );
    }


    @Test
    void upsertServiceActivation_FAIL() {
        //Given
        Activation responseDto = new Activation();
        responseDto.setFiscalCode("EEEEEE00E00E000A");
        responseDto.setStatus("INACTIVE");
        responseDto.setVersion(1);
        responseDto.setServiceId("PN");

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( Activation.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        ActivationPayload fiscalCodePayload = new ActivationPayload();
        fiscalCodePayload.setFiscalCode( "EEEEEE00E00E000A" );
        fiscalCodePayload.setStatus("ACTIVE");
        byte[] reqBodyBites = new byte[0];

        mapper.writerFor( ActivationPayload.class );
        try {
            reqBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "PUT" )
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/activations/" ))
                .respond( response()
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 500 ));


        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/activations/" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        Activation limitedProfile = client.upsertServiceActivation( fiscalCodePayload.getFiscalCode(), true ).block();

        //Then
        assert limitedProfile != null;
        Assertions.assertEquals( "INACTIVE", limitedProfile.getStatus() );
    }

    @Test
    void getServiceActivation() {
        //Given
        Activation responseDto = new Activation();
        responseDto.setFiscalCode("EEEEEE00E00E000A");
        responseDto.setStatus("ACTIVE");
        responseDto.setVersion(1);
        responseDto.setServiceId("PN");

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( Activation.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
        fiscalCodePayload.setFiscalCode( "EEEEEE00E00E000A" );
        byte[] reqBodyBites = new byte[0];

        mapper.writerFor( FiscalCodePayload.class );
        try {
            reqBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/activations/" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        Activation limitedProfile = client.getServiceActivation( fiscalCodePayload.getFiscalCode() ).block();

        //Then
        Assertions.assertNotNull( limitedProfile );
    }
}