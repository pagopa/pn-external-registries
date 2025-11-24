package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.dto.timelineservice.DeliveryInformationResponseInt;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.db.io.dao.IOMessagesDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.IOMessagesEntity;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import it.pagopa.pn.external.registries.services.NotificationService;
import it.pagopa.pn.external.registries.services.TimelineService;
import it.pagopa.pn.external.registries.services.bottomsheet.BottomSheetContext;
import it.pagopa.pn.external.registries.services.bottomsheet.BottomSheetProcessor;
import it.pagopa.pn.external.registries.services.bottomsheet.BottomSheetProcessorFactory;
import it.pagopa.pn.external.registries.services.bottomsheet.ExtendedDeliveryMode;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import it.pagopa.pn.external.registries.util.AppIOUtils;
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
import reactor.test.StepVerifier;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;


@SpringBootTest
class IOServiceTest {

    public static final String IO_REMOTE_CONTENT_CFG_ID = "01HMVMHCZZ8D0VTFWMRHBM5D6F";
    @InjectMocks
    private IOService service;

    @Mock
    IOCourtesyMessageClient ioClient;

    @Mock
    IOOptInClient ioOptinClient;

    @Mock
    IOMessagesDao ioMessagesDao;

    @Mock
    PnExternalRegistriesConfig cfg;

    @Mock
    SendIOSentMessageService ioSentMessageService;

    @Mock
    NotificationService notificationService;

    @Mock
    TimelineService timelineService;

    @Mock
    BottomSheetProcessorFactory bottomSheetProcessorFactory;

    @Mock
    BottomSheetProcessor bottomSheetProcessor;

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
        Mockito.when(bottomSheetProcessorFactory.getBottomSheetProcessor(Mockito.any(BottomSheetContext.class)))
                .thenReturn(bottomSheetProcessor);
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
                .recipientInternalID("PF-123456")
                .recipientIndex(0)
                .creditorTaxId( "creditorTaxId" )
                .subject( "subject" )
                .requestAcceptedDate(OffsetDateTime.now())
                .schedulingAnalogDate(OffsetDateTime.now());
        
        //When
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getIoRemoteContentCfgId() ).thenReturn( IO_REMOTE_CONTENT_CFG_ID );
        Mockito.when( appIoTemplate.getSubjectCourtesyAppIoMessage() ).thenReturn( "Comunicazione a valore legale da {{senderDenomination}}" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        
        Assertions.assertEquals("Comunicazione a valore legale da PaMilano", newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());

        Mockito.verify(ioSentMessageService, Mockito.never()).sendIOSentMessageNotification(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());

    }

    @Test
    void sendIOMessageSuccess_withCC() {
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
                .recipientInternalID("PF-123456")
                .recipientIndex(0)
                .subject( "subject" )
                .carbonCopyToDeliveryPush(true)
                .requestAcceptedDate(OffsetDateTime.now())
                .schedulingAnalogDate(OffsetDateTime.now());

        //When
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( appIoTemplate.getSubjectCourtesyAppIoMessage() ).thenReturn( "Comunicazione a valore legale da {{senderDenomination}}" );
        Mockito.when( cfg.getIoRemoteContentCfgId() ).thenReturn( IO_REMOTE_CONTENT_CFG_ID );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioSentMessageService.sendIOSentMessageNotification(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.any())).thenReturn(Mono.empty());

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();

        Assertions.assertEquals("Comunicazione a valore legale da PaMilano", newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());

        Mockito.verify(ioSentMessageService).sendIOSentMessageNotification(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.any());
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
                .senderDenomination("1111111111" +
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
                        "1111111111")
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .recipientInternalID("PF-123456")
                .recipientIndex(0)
                .creditorTaxId( "creditorTaxId" )
                .subject("a subject")
                .requestAcceptedDate(OffsetDateTime.now());
        
        assert messageRequestDto.getSenderDenomination().length() + messageRequestDto.getSubject().length() > 120;
        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);

        //When
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( appIoTemplate.getSubjectCourtesyAppIoMessage() ).thenReturn( "Comunicazione a valore legale da {{senderDenomination}}" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        String ioSubjectTruncated = ("Comunicazione a valore legale da " + messageRequestDto.getSenderDenomination()).substring(0, 120);

        Assertions.assertEquals(ioSubjectTruncated, newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());
    }

    @Test
    void sendIOActivationAnalogMessageSuccess() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" )
                .deliveryMode(SendMessageRequestDto.DeliveryModeEnum.ANALOG);

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlCittadini() ).thenReturn("https://notifichedigitali.pagopa.it/cittadini");
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_OPTIN, responseDto.getResult());
    }

    @Test
    void sendIOActivationAnalogMessageSuccess_withNullDeliveryModeValue() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" )
                .deliveryMode(null);

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlCittadini() ).thenReturn("https://notifichedigitali.pagopa.it/cittadini");
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_OPTIN, responseDto.getResult());
    }

    @Test
    void sendIOActivationDigitalMessageSuccess() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" )
                .deliveryMode(SendMessageRequestDto.DeliveryModeEnum.DIGITAL);

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoDigitalMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlCittadini() ).thenReturn("https://notifichedigitali.pagopa.it/cittadini");
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_OPTIN, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageNotSent() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_ALREADY_SENT, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageAppioNotActive() {
        //Given
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn(Mono.error(new WebClientResponseException(404, "404 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset())));
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_APPIO_UNAVAILABLE, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageAppioError() {
        //Given
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.now().minus(1, ChronoUnit.DAYS));

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( cfg.getIoOptinMinDays() ).thenReturn( 100 );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn(Mono.error(new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset())));
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.just( createdMessage ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_USER_STATUS, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageMessageError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .subject("subject 123 123 123 123 123")
                .senderDenomination("PaMilano")
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( appIoTemplate.getSubjectCourtesyAppIoMessage() ).thenReturn( "Comunicazione a valore legale da {{senderDenomination}}" );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_COURTESY, responseDto.getResult());
    }

    @Test
    void sendIOActivationAnalogMessageMessageDisabled() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .subject("subject 123 123 123 123 123")
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( false );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_COURTESY_DISABLED_BY_CONF, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageMessageActivationError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" )
                .deliveryMode(SendMessageRequestDto.DeliveryModeEnum.ANALOG);

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( true );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlCittadini() ).thenReturn("https://notifichedigitali.pagopa.it/cittadini");
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.ERROR_OPTIN, responseDto.getResult());
    }


    @Test
    void sendIOActivationAnalogMessageMessageActivationDisabled() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .recipientTaxID( "recipientTaxId" )
                .deliveryMode(SendMessageRequestDto.DeliveryModeEnum.ANALOG);

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoAnalogMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

        IOMessagesEntity ioMessagesEntity = new IOMessagesEntity("123");
        ioMessagesEntity.setLastModified(Instant.EPOCH);

        //When
        Mockito.when( cfg.isEnableIoActivationMessage() ).thenReturn( false );
        Mockito.when( cfg.isEnableIoMessage() ).thenReturn( true );
        Mockito.when( cfg.getAppIoTemplate() ).thenReturn( appIoTemplate );
        Mockito.when( cfg.getPiattaformanotificheurlTos() ).thenReturn( "https://fakeurl.it/tos" );
        Mockito.when( cfg.getPiattaformanotificheurlPrivacy() ).thenReturn( "https://fakeurl.it/privacy" );
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );
        Mockito.when( ioOptinClient.submitMessageforUserWithFiscalCodeInBody( Mockito.any() )).thenReturn( Mono.error( new WebClientResponseException(500, "500 fake", HttpHeaders.EMPTY, new byte[0], Charset.defaultCharset()) ) );
        Mockito.when( ioMessagesDao.get(Mockito.anyString())).thenReturn( Mono.just(ioMessagesEntity) );
        Mockito.when( ioMessagesDao.save(Mockito.any())).thenReturn( Mono.empty() );

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.NOT_SENT_OPTIN_DISABLED_BY_CONF, responseDto.getResult());
    }

    @Test
    void notificationDisclaimerBeforeSchedulingAnalogDateAppIOTest() {
        String recipientInternalId = "internalId";
        String iun = "iun";
        String senderDenomination = "Comune di Milano";
        String subject = "Oggetto del Messaggio";
        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity entity = new IOMessagesEntity();
        entity.setPk(expectedPk);

        PnExternalRegistriesConfig pnConfig = new PnExternalRegistriesConfig();
        pnConfig.init();

        Mockito.when(bottomSheetProcessor.process(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> PreconditionContentInt.builder()
                                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerBeforeAnalogDateAppIoMessage())
                                .title("Questo messaggio contiene una comunicazione a valore legale")
                                .build());

        PreconditionContentInt expected = PreconditionContentInt.builder()
                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerBeforeAnalogDateAppIoMessage())
                .title("Questo messaggio contiene una comunicazione a valore legale")
                .build();

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());

        Mockito.when(timelineService.getDeliveryInformation(iun, 0)).thenReturn(getTimelineServiceResponse(ExtendedDeliveryMode.ANALOG));

        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectNextMatches(actual -> actual.getTitle().equals(expected.getTitle())
                        && actual.getMarkdown().equals(expected.getMarkdown()))
                .verifyComplete();
    }

    @Test
    void notificationDisclaimerAfterSchedulingAnalogDateAppIOTest() {
        String recipientInternalId = "internalId";
        String iun = "iun";

        PnExternalRegistriesConfig pnConfig = new PnExternalRegistriesConfig();
        pnConfig.init();

        Mockito.when(bottomSheetProcessor.process(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> PreconditionContentInt.builder()
                                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerAfterAnalogDateAppIoMessage())
                                .title("Questo messaggio contiene una comunicazione a valore legale")
                                .build());

        PreconditionContentInt expected = PreconditionContentInt.builder()
                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerAfterAnalogDateAppIoMessage())
                .title("Questo messaggio contiene una comunicazione a valore legale")
                .build();

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());
        Mockito.when(timelineService.getDeliveryInformation(iun, 0)).thenReturn(getTimelineServiceResponse(ExtendedDeliveryMode.ANALOG));

        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectNextMatches(actual -> actual.getTitle().equals(expected.getTitle())
                        && actual.getMarkdown().equals(expected.getMarkdown()))
                .verifyComplete();
    }

    @Test
    void notificationDisclaimerDigitalAppIOTest() {
        String recipientInternalId = "internalId";
        String iun = "iun";

        PnExternalRegistriesConfig pnConfig = new PnExternalRegistriesConfig();
        pnConfig.init();

        Mockito.when(bottomSheetProcessor.process(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> PreconditionContentInt.builder()
                                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerDigitalAppIoMessage())
                                .title("Questo messaggio contiene una comunicazione a valore legale")
                                .build());

        PreconditionContentInt expected = PreconditionContentInt.builder()
                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerDigitalAppIoMessage())
                .title("Questo messaggio contiene una comunicazione a valore legale")
                .build();

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());
        Mockito.when(timelineService.getDeliveryInformation(iun, 0)).thenReturn(getTimelineServiceResponse(ExtendedDeliveryMode.DIGITAL));


        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectNextMatches(actual -> actual.getTitle().equals(expected.getTitle())
                        && actual.getMarkdown().equals(expected.getMarkdown()))
                .verifyComplete();
    }

    @Test
    void notificationDisclaimerAfterRefinementAppIOTest() {
        String recipientInternalId = "internalId";
        String iun = "iun";

        PnExternalRegistriesConfig pnConfig = new PnExternalRegistriesConfig();
        pnConfig.init();

        Mockito.when(bottomSheetProcessor.process(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> PreconditionContentInt.builder()
                        .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerAfterRefinementAppIoMessage())
                        .title("Questo messaggio contiene una comunicazione a valore legale")
                        .build());

        PreconditionContentInt expected = PreconditionContentInt.builder()
                .markdown(pnConfig.getAppIoTemplate().getMarkdownDisclaimerAfterRefinementAppIoMessage())
                .title("Questo messaggio contiene una comunicazione a valore legale")
                .build();

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());
        Mockito.when(timelineService.getDeliveryInformation(iun, 0)).thenReturn(getTimelineServiceResponse(ExtendedDeliveryMode.DIGITAL));


        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectNextMatches(actual -> actual.getTitle().equals(expected.getTitle())
                        && actual.getMarkdown().equals(expected.getMarkdown()))
                .verifyComplete();
    }

    @Test
    void notificationDisclaimerWithoutRecipientTest() {
        String recipientInternalId = "notfound";
        String iun = "iun";

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectError(PnNotFoundException.class)
                .verify();
    }

    @Test
    void notificationDisclaimerDeliveryModeUnknownTest() {
        String recipientInternalId = "internalId";
        String iun = "iun";

        Mockito.when(notificationService.getSentNotificationPrivate(iun)).thenReturn(getnotificationServiceResponse());
        Mockito.when(timelineService.getDeliveryInformation(iun, 0)).thenReturn(getTimelineServiceResponse(ExtendedDeliveryMode.UNKNOWN));

        Mono<PreconditionContentDto> result = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(result)
                .expectError(PnNotFoundException.class)
                .verify();
    }

    private Mono<DeliveryInformationResponseInt> getTimelineServiceResponse(ExtendedDeliveryMode deliveryMode) {
        return Mono.just(DeliveryInformationResponseInt.builder()
                .isNotificationCancelled(false)
                .refinementOrViewedDate(Instant.now())
                .deliveryMode(deliveryMode)
                .schedulingAnalogDate(Instant.now()
                ).build());
    }

    private Mono<NotificationInt> getnotificationServiceResponse() {
        return Mono.just(NotificationInt.builder()
                        .iun("iun")
                        .senderDenomination("Oggetto del Messaggio")
                        .recipients(getRecipientsList())
                        .subject("subject")
                .build());
    }

    private List<NotificationRecipientInt> getRecipientsList() {
        return List.of(NotificationRecipientInt.builder()
                .internalId("internalId")
                .build());
    }
}