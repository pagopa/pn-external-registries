package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class MVPValidUserServiceTest {

    private static final String TAX_ID = "EEEEEE00E00E000A";
    @InjectMocks
    private MVPValidUserService service;

    @Mock
    IOOptInClient ioClient;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public SqsNotificationPaidProducer sqsNotificationPaidProducer() {
            return Mockito.mock( SqsNotificationPaidProducer.class);
        }
    }

    @Test
    void checkValidUserPnActive() {
        // Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        // When
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );

        MvpUserDto mvpUserDto = service.checkValidUser( Mono.just( TAX_ID ) ).block();

        // Then
        Assertions.assertNotNull( mvpUserDto );
        Assertions.assertEquals( TAX_ID, mvpUserDto.getTaxId() );
        Assertions.assertTrue(mvpUserDto.getValid());
    }

    @Test
    void checkValidUserPnNotActive() {
        // Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( false )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        // When
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );

        MvpUserDto mvpUserDto = service.checkValidUser( Mono.just( TAX_ID ) ).block();

        // Then
        Assertions.assertNotNull( mvpUserDto );
        Assertions.assertEquals( TAX_ID, mvpUserDto.getTaxId() );
        Assertions.assertTrue(mvpUserDto.getValid());
    }
    
}