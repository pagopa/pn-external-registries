package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOActivationClient;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static it.pagopa.pn.external.registries.middleware.msclient.io.IOActivationClient.IO_STATUS_ACTIVE;
import static it.pagopa.pn.external.registries.middleware.msclient.io.IOActivationClient.IO_STATUS_INACTIVE;

@SpringBootTest
class MVPValidUserServiceTest {

    private static final String TAX_ID = "EEEEEE00E00E000A";
    @InjectMocks
    private MVPValidUserService service;

    @Mock
    IOActivationClient ioActivationClient;

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
        Activation activation = new Activation();
        activation.setVersion(1);
        activation.setStatus(IO_STATUS_ACTIVE);
        activation.setFiscalCode(TAX_ID);

        // When
        Mockito.when( ioActivationClient.getServiceActivation( Mockito.any() ) ).thenReturn( Mono.just( activation ) );

        MvpUserDto mvpUserDto = service.checkValidUser( Mono.just( TAX_ID ) ).block();

        // Then
        Assertions.assertNotNull( mvpUserDto );
        Assertions.assertEquals( TAX_ID, mvpUserDto.getTaxId() );
        Assertions.assertTrue(mvpUserDto.getValid());
    }

    @Test
    void checkValidUserPnNotActive() {
        // Given
        Activation activation = new Activation();
        activation.setVersion(1);
        activation.setStatus(IO_STATUS_INACTIVE);
        activation.setFiscalCode(TAX_ID);


        // When
        Mockito.when( ioActivationClient.getServiceActivation( Mockito.any() ) ).thenReturn( Mono.just( activation ) );

        MvpUserDto mvpUserDto = service.checkValidUser( Mono.just( TAX_ID ) ).block();

        // Then
        Assertions.assertNotNull( mvpUserDto );
        Assertions.assertEquals( TAX_ID, mvpUserDto.getTaxId() );
        Assertions.assertFalse(mvpUserDto.getValid());
    }
    
}