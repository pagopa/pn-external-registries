package it.pagopa.pn.external.registries.pdnd.service;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = {
        AssertionGenerator.class, AwsConfigs.class, AwsServicesClientsConfig.class,
        PnExternalRegistriesConfig.class, AccessTokenCacheService.class, PDNDClient.class
})
@Slf4j
public class AccessTokenCacheServiceIT {

    @Autowired
    private AccessTokenCacheService tokenService;

    @Autowired
    private PDNDClient client;

    @Autowired
    private PnExternalRegistriesConfig config;

    @Test
    void tokenServiceTest() {

        String a = tokenService.getToken("M2", false).block();
        log.info("TEST -> received access token {}", a);
        assertNotNull(a);
    }
}
