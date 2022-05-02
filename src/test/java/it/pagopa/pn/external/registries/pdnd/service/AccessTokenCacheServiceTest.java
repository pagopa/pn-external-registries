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
    AccessTokenCacheService tokenService;

    @MockBean
    PDNDClient pdndClient;

    @DisplayName("Simulazione richiesta TOKEN")
    @Test
    public void tokenServiceTest() throws Exception {
        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(600);

        Mockito.when(pdndClient.createToken()).thenReturn(Mono.just(resp));

        String a = tokenService.getToken("M1",false).block();
        assertNotNull(a);
        if (a != null) {
            log.info("TEST -> received access token " + a);
        }
        assertNotNull(a);
    }

    @DisplayName("Simulazione richiesta 2 TOKEN prima della scadenza del timeut")
    @Test
    public void tokenServiceTestWithoutRefresh() throws Exception {
        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(600);

        Mockito.when(pdndClient.createToken()).thenReturn(Mono.just(resp));

        String a = tokenService.getToken("M2",false).block();
        if (a != null) {
            log.info("TEST -> received access token " + a);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String b = tokenService.getToken("M2",false).block();
        if (b != null) {
            log.info("TEST -> received access token " + a);
        }
        assertEquals(a,b);
    }

    @DisplayName("Simulazione richiesta 2 TOKEN dopo la scadenza del timeut")
    @Test
    public void tokenServiceTestWithRefresh() throws Exception {
        ClientCredentialsResponseDto resp = new ClientCredentialsResponseDto();
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        resp.expiresIn(5);

        Mockito.when(pdndClient.createToken()).thenReturn(Mono.just(resp));

        String a = tokenService.getToken("M3",false).block();
        if (a != null) {
            log.info("TEST -> received access token " + a);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resp.accessToken("MY_ACCESS_TOKEN "+ new Date());
        Mockito.when(pdndClient.createToken()).thenReturn(Mono.just(resp));

        String b = tokenService.getToken("M3",false).block();
        if (b != null) {
            log.info("TEST -> received access token " + b);
        }
        assertNotEquals(a,b);
    }
}
