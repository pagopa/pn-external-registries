package it.pagopa.pn.external.registries.pdnd.service;

import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;

import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class AccessTokenCacheServiceTest {


    @Autowired
    private AccessTokenCacheService tokenService;

    @MockBean
    private PDNDClient pdndClient;

    @DisplayName("Simulazione richiesta token, devono essere uguali")
    @Test
    public void tokenServiceTestKeepCache() throws Exception {

        // - GIVEN
        String purposeId = "test1";

        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(600);

        Mockito.when(pdndClient.createToken( purposeId )).thenReturn(Mono.just(resp));

        // - WHEN
        String accessToken1 = tokenService.getToken( purposeId,false).block();
        String accessToken2 = tokenService.getToken( purposeId,false).block();

        // - THEN
        assertNotNull( accessToken1 );
        assertNotNull( accessToken2 );

        assertEquals( resp.getAccessToken(), accessToken1 );
        assertEquals( resp.getAccessToken(), accessToken2 );

        Mockito.verify( pdndClient, Mockito.times(1) ).createToken( purposeId );
    }

    @DisplayName("Simulazione scadenza token")
    @Test
    public void tokenServiceTestWithoutRefresh() throws Exception {
        // - GIVEN
        String purposeId = "test2";

        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(1);

        Mockito.when(pdndClient.createToken( purposeId )).thenReturn(Mono.just(resp));

        // - WHEN
        String accessToken1 = tokenService.getToken( purposeId,false).block();
        Thread.sleep( 2000 );
        String accessToken2 = tokenService.getToken( purposeId,false).block();

        // - THEN
        assertNotNull( accessToken1 );
        assertNotNull( accessToken2 );

        Mockito.verify( pdndClient, Mockito.times(2) ).createToken( purposeId );
    }

    @DisplayName("Simulazione refresh token")
    @Test
    public void tokenServiceTestForceRefresh() throws Exception {
        // - GIVEN
        String purposeId = "test3";

        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(1);

        Mockito.when(pdndClient.createToken( purposeId )).thenReturn(Mono.just(resp));

        // - WHEN
        String accessToken1 = tokenService.getToken( purposeId,false).block();
        String accessToken2 = tokenService.getToken( purposeId,true).block();

        // - THEN
        assertNotNull( accessToken1 );
        assertNotNull( accessToken2 );

        Mockito.verify( pdndClient, Mockito.times(2) ).createToken( purposeId );
    }

}
