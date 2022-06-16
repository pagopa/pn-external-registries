package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.IO-base-url=http://localhost:9999",
        "pn.external-registry.IO-api-key=fake_api_key"
})
class IOClientTest {

    @Autowired
    private IOClient client;

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
    void getProfileByPOST() {
        //Given
        LimitedProfile responseDto = new LimitedProfile()
                .preferredLanguages(Collections.singletonList( "it_IT" ))
                .senderAllowed( true );

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( LimitedProfile.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }


        FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
        fiscalCodePayload.setFiscalCode( "EEEEEE00E00E000A" );

        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withPath( "/profiles" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        LimitedProfile limitedProfile = client.getProfileByPOST( fiscalCodePayload ).block();

        //Then
        Assertions.assertNotNull( limitedProfile );
    }

    @Test
    void submitMessageforUserWithFiscalCodeInBody() {
        //Given
        MessageContent messageContent = new MessageContent()
                .dueDate( "2018-10-13T00:00:00.000Z" )
                .markdown( "Per visualizzare il contenuto del certificato,  **aggiorna IO all'ultima versione disponibile**:\\n\\n- [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\\n\\n- [Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)\\n\\n***\\n\\nIn order to visualize your EU Digital Covid Certificate, **you have to update IO to the last available version**:\\n\\n- [Update for Android devices](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\\n\\n- [Update for iOS devices](https://apps.apple.com/it/app/io/id1501681835)" )
                /*.paymentData( new PaymentData()
                        .amount( 9999999L )
                        .noticeNumber( "331613939824840064" )
                        .payee( new Payee()
                                .fiscalCode( "EEEEEE00E00E000A" ))
                )*/
                .subject( "Comune di Milano: infrazione della strada" )
                /*.thirdPartyData( new ThirdPartyData()
                        .id( "IUN" )
                        .originalSender( "Comune di Milano" )
                        .originalReceiptDate( "2022-06-14T00:00:00.000Z" )
                        .hasAttachments( true )
               .summary( "Infrazione della strada" ))*/;
        NewMessage message = new NewMessage()
                .fiscalCode( "EEEEEE00E00E000A" )
                .content( messageContent );

        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        byte[] responseBodyBites = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( CreatedMessage.class );
        try {
            responseBodyBites = mapper.writeValueAsBytes( createdMessage );
        } catch ( JsonProcessingException e ){
            e.printStackTrace();
        }

        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withPath( "/messages" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        CreatedMessage createdMessageResult = client.submitMessageforUserWithFiscalCodeInBody( message ).block();

        //Then
        Assertions.assertNotNull( createdMessageResult );
    }

}