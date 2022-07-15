package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;

@SpringBootTest
class SendIOMessageServiceTest {

    @InjectMocks
    private IOService service;

    @Mock
    IOClient ioClient;

    @Mock
    PnExternalRegistriesConfig cfg;

    @BeforeEach
    public void init(){
/*
        cfg = new PnExternalRegistriesConfig();
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = new PnExternalRegistriesConfig.AppIoTemplate();
        appIoTemplate.setMarkdownActivationAppIoMessage("markdownActivationAppIoMessage");
        appIoTemplate.setMarkdownUpgradeAppIoMessage("markdownUpgradeAppIoMessage");
        appIoTemplate.setSubjectActivationAppIoMessage("subjectActivationAppIoMessage");
        cfg.setAppIoTemplate(appIoTemplate);
        cfg.setEnableIoMessage(true);
        cfg.setEnableIoActivationMessage(true);

        service = new SendIOMessageService(ioClient, cfg);*/
    }

    @Test
    void sendIOMessageSuccess() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .amount( 2000 )
                .creditorTaxId( "creditorTaxId" )
                .dueDate( OffsetDateTime.ofInstant( Instant.now(), ZoneId.of( "UTC" ) ) )
                .iun( "iun" )
                .senderDenomination("PaMilano")
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .creditorTaxId( "creditorTaxId" )
                .subject( "subject" )
                .requestAcceptedDate(OffsetDateTime.now());
        
        //When
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        String ioSubject = messageRequestDto.getSubject() + "-" + messageRequestDto.getSenderDenomination();
        
        Assertions.assertEquals(ioSubject, newMessage.getContent().getSubject());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());

    }

    @Test
    void sendIOMessageSuccessSubjectExceeds() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .amount( 2000 )
                .creditorTaxId( "creditorTaxId" )
                .dueDate( OffsetDateTime.ofInstant( Instant.now(), ZoneId.of( "UTC" ) ) )
                .iun( "iun" )
                .senderDenomination("PaMilano")
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .creditorTaxId( "creditorTaxId" )
                .subject( 
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" +
                        "1111111111" )
                .requestAcceptedDate(OffsetDateTime.now());
        
        assert messageRequestDto.getSenderDenomination().length() + messageRequestDto.getSubject().length() > 120;
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        //When
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        String ioSubject = messageRequestDto.getSubject() + "-" + messageRequestDto.getSenderDenomination();
        String ioSubjectTruncated = ioSubject.substring(0, 120);

        Assertions.assertEquals(ioSubjectTruncated, newMessage.getContent().getSubject());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());
    }

    @Test
    void sendIOActivationMessageSuccess() {
        //Given
/*        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendActivationMessageRequestDto messageRequestDto = new SendActivationMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitActivationMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );

        SendMessageResponseDto responseDto = service.sendIOActivationMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );*/


    }


}