package it.pagopa.pn.external.registries.pdnd.service;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
@Slf4j
public class AccessTokenCacheServiceIntegrationTest {
    @Autowired
    AccessTokenCacheService tokenService;

    @Autowired
    PDNDClient client;

    @Autowired
    PnExternalRegistriesConfig config;

    @Test
    public void tokenServiceTest(){

        String a = tokenService.getToken("M2").block();

        if (a != null) {
            log.info("TEST -> received access token " +a);
        }
       assertNotNull(a);
    }
}
