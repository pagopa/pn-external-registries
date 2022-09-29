package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.UserStatusResponseDto;
import it.pagopa.pn.external.registries.middleware.db.io.dao.OptInSentDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.OptInSentEntity;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@SpringBootTest
class IOServiceTest {

    @InjectMocks
    private IOService service;

    @Mock
    IOCourtesyMessageClient ioClient;

    @Mock
    IOOptInClient ioOptinClient;

    @Mock
    OptInSentDao optInSentDao;

    @Mock
    PnExternalRegistriesConfig cfg;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public SqsNotificationPaidProducer sqsNotificationPaidProducer() {
            return Mockito.mock( SqsNotificationPaidProducer.class);
        }
    }

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
    void getUserStatus() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        UserStatusRequestDto userStatusRequestDto = new UserStatusRequestDto()
                .taxId("123123123");

        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        //When

        UserStatusResponseDto responseDto = service.getUserStatus( Mono.just( userStatusRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals(UserStatusResponseDto.StatusEnum.PN_ACTIVE, responseDto.getStatus());
    }

    @Test
    void getUserStatusNotActive() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        UserStatusRequestDto userStatusRequestDto = new UserStatusRequestDto()
                .taxId("123123123");

        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        //When

        UserStatusResponseDto responseDto = service.getUserStatus( Mono.just( userStatusRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals(UserStatusResponseDto.StatusEnum.PN_NOT_ACTIVE, responseDto.getStatus());

    }

    @Test
    void getUserStatusAppIONotAvailable() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        UserStatusRequestDto userStatusRequestDto = new UserStatusRequestDto()
                .taxId("123123123");

        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.error( new WebClientResponseException(404, "404 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        //When

        UserStatusResponseDto responseDto = service.getUserStatus( Mono.just( userStatusRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals(UserStatusResponseDto.StatusEnum.APPIO_NOT_ACTIVE, responseDto.getStatus());

    }


    @Test
    void getUserStatusAppIOError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        UserStatusRequestDto userStatusRequestDto = new UserStatusRequestDto()
                .taxId("123123123");

        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        //When

        UserStatusResponseDto responseDto = service.getUserStatus( Mono.just( userStatusRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals(UserStatusResponseDto.StatusEnum.ERROR, responseDto.getStatus());

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
        String ioSubject = messageRequestDto.getSubject();
        
        Assertions.assertEquals(ioSubject, newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
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
        String ioSubject = messageRequestDto.getSubject();
        String ioSubjectTruncated = ioSubject.substring(0, 120);

        Assertions.assertEquals(ioSubjectTruncated, newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());
    }

    @Test
    void sendIOActivationMessageSuccess() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_OPTIN, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageNotSent() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_ALREADY_SENT, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageAppioNotActive() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn(Mono.error(new WebClientResponseException(404, "404 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset())));
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_APPIO_UNAVAILABLE, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageAppioError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn(Mono.error(new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset())));
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_USER_STATUS, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageMessageError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .subject("subject 123 123 123 123 123")
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_COURTESY, responseDto.getResult());


    }

    @Test
    void sendIOActivationMessageMessageDisabled() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .subject("subject 123 123 123 123 123")
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( false );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_COURTESY_DISABLED_BY_CONF, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageMessageActivationError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_OPTIN, responseDto.getResult());


    }


    @Test
    void sendIOActivationMessageMessageActivationDisabled() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        OptInSentEntity optInSentEntity = new OptInSentEntity("123");
        optInSentEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( false );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( optInSentDao.get(Mockito.anyString())).thenReturn( Mono.just( optInSentEntity ) );
        Mockito.when( optInSentDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_DISABLED_BY_CONF, responseDto.getResult());


    }

}