package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.deliverypush.v1.dto.ProbableSchedulingAnalogDateResponse;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.db.io.dao.IOMessagesDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.IOMessagesEntity;
import it.pagopa.pn.external.registries.middleware.msclient.DeliveryPushClient;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
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
import java.util.Map;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

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
    DeliveryPushClient deliveryPushClient;

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
        Mockito.when(cfg.getDeliveryPushBaseUrl()).thenReturn("http://localhost:8081");
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
        Mockito.when( ioMessagesDao.save(Mockito.any(IOMessagesEntity.class)) ).thenReturn(Mono.empty());

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

        ArgumentCaptor<IOMessagesEntity> ioMessagesEntityCaptor = ArgumentCaptor.forClass(IOMessagesEntity.class);
        Mockito.verify(ioMessagesDao, Mockito.times(1)).save(ioMessagesEntityCaptor.capture());

        // verifico che è stato inserito il record per il ioMessagesEntity (probableSchedulingAnalogDate)
        assertThat(ioMessagesEntityCaptor.getValue().getPk()).isEqualTo("SENT##" + messageRequestDto.getIun() + "##" + messageRequestDto.getRecipientInternalID());
        assertThat(ioMessagesEntityCaptor.getValue().getSchedulingAnalogDate()).isEqualTo(messageRequestDto.getSchedulingAnalogDate().toInstant());
        assertThat(ioMessagesEntityCaptor.getValue().getTtl()).isEqualTo(messageRequestDto.getSchedulingAnalogDate().toInstant().plus(2, ChronoUnit.DAYS).getEpochSecond());
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
        Mockito.when(ioMessagesDao.save(Mockito.any(IOMessagesEntity.class))).thenReturn(Mono.empty());

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        String ioSubject = messageRequestDto.getSubject();

        Assertions.assertEquals("Comunicazione a valore legale da PaMilano", newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());

        Mockito.verify(ioSentMessageService).sendIOSentMessageNotification(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.any());

        ArgumentCaptor<IOMessagesEntity> ioMessagesEntityCaptor = ArgumentCaptor.forClass(IOMessagesEntity.class);
        Mockito.verify(ioMessagesDao, Mockito.times(1)).save(ioMessagesEntityCaptor.capture());

        // verifico che è stato inserito il record per il ioMessagesEntity (probableSchedulingAnalogDate)
        assertThat(ioMessagesEntityCaptor.getValue().getPk()).isEqualTo("SENT##" + messageRequestDto.getIun() + "##" + messageRequestDto.getRecipientInternalID());
        assertThat(ioMessagesEntityCaptor.getValue().getSchedulingAnalogDate()).isEqualTo(messageRequestDto.getSchedulingAnalogDate().toInstant());
        assertThat(ioMessagesEntityCaptor.getValue().getTtl()).isEqualTo(messageRequestDto.getSchedulingAnalogDate().toInstant().plus(2, ChronoUnit.DAYS).getEpochSecond());
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
        Mockito.when(ioMessagesDao.save(Mockito.any(IOMessagesEntity.class))).thenReturn(Mono.empty());

        SendMessageResponseDto responseDto = service.sendIOMessage( Mono.just( messageRequestDto ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );

        ArgumentCaptor<NewMessage> newMessageCaptor = ArgumentCaptor.forClass(NewMessage.class);
        Mockito.verify(ioClient).submitMessageforUserWithFiscalCodeInBody(newMessageCaptor.capture());
        NewMessage newMessage = newMessageCaptor.getValue();
        String ioSubject = messageRequestDto.getSubject();
        String ioSubjectTruncated = ("Comunicazione a valore legale da " + messageRequestDto.getSenderDenomination()).substring(0, 120);

        Assertions.assertEquals(ioSubjectTruncated, newMessage.getContent().getSubject());
        Assertions.assertEquals( SendMessageResponseDto.ResultEnum.SENT_COURTESY, responseDto.getResult());
        Assertions.assertNotNull(newMessage.getContent().getThirdPartyData());
        Assertions.assertEquals(messageRequestDto.getSubject(), newMessage.getContent().getThirdPartyData().getSummary());

        Mockito.verify(ioMessagesDao, Mockito.times(1)).save(Mockito.any());

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
    void sendIOActivationMessageMessageError() {
        //Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));
        CreatedMessage createdMessage = new CreatedMessage()
                .id( "createdMessageId" );

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .subject("subject 123 123 123 123 123")
                .senderDenomination("PaMilano")
                .recipientTaxID( "recipientTaxId" );

        PnExternalRegistriesConfig.AppIoTemplate appIoTemplate = Mockito.mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        Mockito.when(appIoTemplate.getMarkdownActivationAppIoMessage()).thenReturn("ciao, attiva piattaforma notifiche ${piattaformaNotificheURLTOS} ${piattaformaNotificheURLPrivacy}");

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
    void notificationDisclaimerBeforeSchedulingAnalogDateTest() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(Instant.parse("2050-05-03T13:51:00Z"));
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        PreconditionContentDto expectedResponse = new PreconditionContentDto()
                .messageCode(PRE_ANALOG_MESSAGE_CODE)
                        .title(PRE_ANALOG_TITLE)
                                .markdown(pnExternalRegistriesConfig.getAppIoTemplate().getMarkdownDisclaimerBeforeDateAppIoMessage()
                                        .replace(DATE_PLACEHOLDER, "03-05-2050")
                                        .replace(TIME_PLACEHOLDER, "15:51") //Europe/Rome
                                        .replace(IUN_PLACEHOLDER, iun)
                                        .replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination)
                                        .replace(SUBJECT_PLACEHOLDER, subject)
                                )
                .messageParams(Map.of(
                        DATE_MESSAGE_PARAM, "03-05-2050",
                        TIME_MESSAGE_PARAM, "13:51", //UTC
                        IUN_PARAM, iun,
                        SENDER_DENOMINATION_PARAM, senderDenomination,
                        SUBJECT_PARAM, subject
                ));

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(deliveryPushClient, Mockito.times(0)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);


    }

    @Test
    void notificationDisclaimerAfterSchedulingAnalogDateTest() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(Instant.parse("2023-05-02T13:51:00Z"));
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        PreconditionContentDto expectedResponse = new PreconditionContentDto()
                .messageCode(POST_ANALOG_MESSAGE_CODE)
                .title(POST_ANALOG_TITLE)
                .markdown(pnExternalRegistriesConfig.getAppIoTemplate().getMarkdownDisclaimerAfterDateAppIoMessage()
                        .replace(IUN_PLACEHOLDER, iun)
                        .replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination)
                        .replace(SUBJECT_PLACEHOLDER, subject)
                )
                .messageParams(Map.of(
                        IUN_PARAM, iun,
                        SENDER_DENOMINATION_PARAM, senderDenomination,
                        SUBJECT_PARAM, subject
                ));

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(deliveryPushClient, Mockito.times(0)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);

    }

    @Test
    void notificationDisclaimerWithoutSchedulingAnalogDateAndDeliveryPush404Test() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(null);
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        PreconditionContentDto expectedResponse = new PreconditionContentDto()
                .messageCode(POST_ANALOG_MESSAGE_CODE)
                .title(POST_ANALOG_TITLE)
                .markdown(pnExternalRegistriesConfig.getAppIoTemplate().getMarkdownDisclaimerAfterDateAppIoMessage()
                        .replace(IUN_PLACEHOLDER, iun)
                        .replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination)
                        .replace(SUBJECT_PLACEHOLDER, subject)
                )
                .messageParams(Map.of(
                        IUN_PARAM, iun,
                        SENDER_DENOMINATION_PARAM, senderDenomination,
                        SUBJECT_PARAM, subject
                ));

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());
        Mockito.when(deliveryPushClient.getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId)).thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", new HttpHeaders(), null, Charset.defaultCharset())));

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(deliveryPushClient, Mockito.times(1)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);

    }

    @Test
    void notificationDisclaimerWithoutSchedulingAnalogDateAndDeliveryPush400Test() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(null);
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();


        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());
        Mockito.when(deliveryPushClient.getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId)).thenReturn(Mono.error(WebClientResponseException.create(400, "Bad Request", new HttpHeaders(), null, Charset.defaultCharset())));

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectError()
                .verify();

        Mockito.verify(deliveryPushClient, Mockito.times(1)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);

    }

    @Test
    void notificationDisclaimerWithoutSchedulingAnalogDateAndDeliveryPush200WithBeforeFlowTest() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(null);
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        PreconditionContentDto expectedResponse = new PreconditionContentDto()
                .messageCode(PRE_ANALOG_MESSAGE_CODE)
                .title(PRE_ANALOG_TITLE)
                .markdown(pnExternalRegistriesConfig.getAppIoTemplate().getMarkdownDisclaimerBeforeDateAppIoMessage()
                        .replace(DATE_PLACEHOLDER, "03-05-2050")
                        .replace(TIME_PLACEHOLDER, "15:51") //Europe/ROme
                        .replace(IUN_PLACEHOLDER, iun)
                        .replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination)
                        .replace(SUBJECT_PLACEHOLDER, subject)
                )
                .messageParams(Map.of(
                        DATE_MESSAGE_PARAM, "03-05-2050",
                        TIME_MESSAGE_PARAM, "13:51", //UTC
                        IUN_PARAM, iun,
                        SENDER_DENOMINATION_PARAM, senderDenomination,
                        SUBJECT_PARAM, subject
                ));

        ProbableSchedulingAnalogDateResponse deliveryPushResponse = new ProbableSchedulingAnalogDateResponse()
                .iun(iun)
                .recIndex(0)
                .schedulingAnalogDate(Instant.parse("2050-05-03T13:51:00Z"));

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());
        Mockito.when(deliveryPushClient.getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId)).thenReturn(Mono.just(deliveryPushResponse));

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(deliveryPushClient, Mockito.times(1)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);

    }

    @Test
    void notificationDisclaimerWithoutSchedulingAnalogDateAndDeliveryPush200WithAfterFlowTest() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";
        final String senderDenomination = "Comune di Milano";
        final String subject = "Oggetto del Messaggio";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        IOMessagesEntity expectedEntity = new IOMessagesEntity();
        expectedEntity.setPk(expectedPk);
        expectedEntity.setSchedulingAnalogDate(null);
        expectedEntity.setIun(iun);
        expectedEntity.setSenderDenomination(senderDenomination);
        expectedEntity.setSubject(subject);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        PreconditionContentDto expectedResponse = new PreconditionContentDto()
                .messageCode(POST_ANALOG_MESSAGE_CODE)
                .title(POST_ANALOG_TITLE)
                .markdown(pnExternalRegistriesConfig.getAppIoTemplate().getMarkdownDisclaimerAfterDateAppIoMessage()
                        .replace(IUN_PLACEHOLDER, iun)
                        .replace(SENDER_DENOMINATION_PLACEHOLDER, senderDenomination)
                        .replace(SUBJECT_PLACEHOLDER, subject)
                )
                .messageParams(Map.of(
                        IUN_PARAM, iun,
                        SENDER_DENOMINATION_PARAM, senderDenomination,
                        SUBJECT_PARAM, subject
                ));

        ProbableSchedulingAnalogDateResponse deliveryPushResponse = new ProbableSchedulingAnalogDateResponse()
                .iun(iun)
                .recIndex(0)
                .schedulingAnalogDate(Instant.parse("1999-05-05T10:00:00Z"));

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.just(expectedEntity));
        Mockito.when(cfg.getAppIoTemplate()).thenReturn(pnExternalRegistriesConfig.getAppIoTemplate());
        Mockito.when(deliveryPushClient.getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId)).thenReturn(Mono.just(deliveryPushResponse));

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(deliveryPushClient, Mockito.times(1)).getSchedulingAnalogDateWithHttpInfo(iun, recipientInternalId);

    }

    @Test
    void notificationDisclaimerWithoutRecordInDBTest() {
        final String recipientInternalId = "internalId";
        final String iun = "iun";

        String expectedPk = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        //voglio che il mock di PnExternalRegistriesConfig si comporti come la classe reale
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.init();

        Mockito.when(ioMessagesDao.get(expectedPk)).thenReturn(Mono.empty());

        Mono<PreconditionContentDto> actualMonoResponse = service.notificationDisclaimer(recipientInternalId, iun);

        StepVerifier.create(actualMonoResponse)
                .expectError(PnNotFoundException.class)
                .verify();


    }
}