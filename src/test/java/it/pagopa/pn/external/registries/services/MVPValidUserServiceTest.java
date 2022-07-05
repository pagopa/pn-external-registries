package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.IOClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MVPValidUserServiceTest {

    private static final String TAX_ID = "EEEEEE00E00E000A";
    @InjectMocks
    private MVPValidUserService service;

    @Mock
    IOClient ioClient;

    @Test
    void checkUser() {
        // Given
        LimitedProfile limitedProfile = new LimitedProfile()
                .senderAllowed( true )
                .preferredLanguages(Collections.singletonList( "IT-It" ));

        // When
        Mockito.when( ioClient.getProfileByPOST( Mockito.any() ) ).thenReturn( Mono.just( limitedProfile ) );

        MvpUserDto mvpUserDto = service.checkValidUser( Mono.just( TAX_ID ) ).block();

        // Then
        Assertions.assertNotNull( mvpUserDto );
    }

}