package it.pagopa.pn.external.registries.middleware.msclient.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOClient;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.io-base-url=http://localhost:9999",
        "pn.external-registry.io-api-key=fake_api_key",
        "pn.external-registry.ioact-api-key=fake_api_key_activation"
})
class IOClientTest {

    @Autowired
    private IOClient client;

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
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
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
    void getProfileByPOST_mocked() {
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
        fiscalCodePayload.setFiscalCode( "CSRGGL44L13H501E" );

        new MockServerClient( "localhost", 9999 )
                .when( request()
                        .withMethod( "POST" )
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/profiles" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        Assertions.assertThrows(WebClientResponseException.class, () -> client.getProfileByPOST( fiscalCodePayload ).block());

        //Then
    }

    @Test
    void submitMessageforUserWithFiscalCodeInBody() {
        //Given
        MessageContent messageContent = new MessageContent()
                .dueDate( "2018-10-13T00:00:00.000Z" )
                //.markdown( "Ciao,\n\nper ricevere messaggi su IO dal servizio \"Avvisi di cortesia\" di Piattaforma Notifiche, devi **aggiornare l'app all'ultima versione disponibile**:\n\n [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)\n\n***\n\nIn order to receive message from service \"Avvisi di cortesia\" of Piattaforma Notifiche, **you have to update the app to the last available version**:\n\n [Update for Android devices](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Update for iOS devices](https://apps.apple.com/it/app/io/id1501681835)" )
                .markdown( "Ciao,\n\nper ricevere messaggi su IO dal servizio \"Avvisi di cortesia\" di Piattaforma Notifiche, devi **aggiornare l'app all'ultima versione disponibile**:\n\n [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)" )
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
                .featureLevelType("ADVANCED")
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
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
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


    @Test
    void submitMessageforUserWithFiscalCodeInBody_mocked() {
        //Given
        MessageContent messageContent = new MessageContent()
                .dueDate( "2018-10-13T00:00:00.000Z" )
                //.markdown( "Ciao,\n\nper ricevere messaggi su IO dal servizio \"Avvisi di cortesia\" di Piattaforma Notifiche, devi **aggiornare l'app all'ultima versione disponibile**:\n\n [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)\n\n***\n\nIn order to receive message from service \"Avvisi di cortesia\" of Piattaforma Notifiche, **you have to update the app to the last available version**:\n\n [Update for Android devices](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Update for iOS devices](https://apps.apple.com/it/app/io/id1501681835)" )
                .markdown( "Ciao,\n\nper ricevere messaggi su IO dal servizio \"Avvisi di cortesia\" di Piattaforma Notifiche, devi **aggiornare l'app all'ultima versione disponibile**:\n\n [Aggiorna per dispositivi Android](https://play.google.com/store/apps/details?id=it.pagopa.io.app)\n\n[Aggiorna per dispositivi iOS](https://apps.apple.com/it/app/io/id1501681835)" )
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
                .fiscalCode( "CSRGGL44L13H501E" )
                .featureLevelType("ADVANCED")
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
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key")
                        .withPath( "/messages" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 500 ));

        //When
        CreatedMessage createdMessageResult = client.submitMessageforUserWithFiscalCodeInBody( message ).block();

        //Then
        Assertions.assertNotNull( createdMessageResult );
    }

    @Test
    void submitActivationMessageforUserWithFiscalCodeInBody() {
        //Given
        MessageContent messageContent = new MessageContent()
                .markdown( "markdown di attivazione" )
                .subject( "attiva piattaforma notifiche" );

        NewMessage message = new NewMessage()
                .fiscalCode( "EEEEEE00E00E000A" )
                .featureLevelType("ADVANCED")
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
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key_activation")
                        .withPath( "/messages" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 200 ));

        //When
        CreatedMessage createdMessageResult = client.submitActivationMessageforUserWithFiscalCodeInBody( message ).block();

        //Then
        Assertions.assertNotNull( createdMessageResult );
    }


    @Test
    void submitActivationMessageforUserWithFiscalCodeInBody_mocked() {
        //Given
        MessageContent messageContent = new MessageContent()
                .markdown( "markdown di attivazione" )
                .subject( "attiva piattaforma notifiche" );

        NewMessage message = new NewMessage()
                .fiscalCode( "CSRGGL44L13H501E" )
                .featureLevelType("ADVANCED")
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
                        .withHeader("Ocp-Apim-Subscription-Key", "fake_api_key_activation")
                        .withPath( "/messages" ))
                .respond( response()
                        .withBody( responseBodyBites )
                        .withContentType( MediaType.APPLICATION_JSON )
                        .withStatusCode( 500 ));

        //When
        CreatedMessage createdMessageResult = client.submitActivationMessageforUserWithFiscalCodeInBody( message ).block();

        //Then
        Assertions.assertNotNull( createdMessageResult );
    }


}