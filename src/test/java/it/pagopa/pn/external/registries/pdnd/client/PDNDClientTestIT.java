package it.pagopa.pn.external.registries.pdnd.client;

import it.pagopa.pn.external.registries.config.AccessTokenConfig;
import it.pagopa.pn.external.registries.config.JwtConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.pdnd.service.AccessTokenCacheService;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGenerator;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
public class PDNDClientTestIT {

    private PDNDClient pdndClient;

    @BeforeEach
    void setup() {

        AwsConfigs awsCfg = new AwsConfigs();
        awsCfg.setProfileName("staging");

        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setClientAssertionTTL( 1000 );

        AccessTokenConfig accessTokenCfg = new AccessTokenConfig();
        accessTokenCfg.setJwtCfg( jwtConfig );

        PnExternalRegistriesConfig config = new PnExternalRegistriesConfig();
        config.setPdndM2m( accessTokenCfg );


        AssertionGenerator ag = new AssertionGenerator( new AwsServicesClientsConfig( awsCfg ).kmsAsyncClient() );

        pdndClient = new PDNDClient( config, ag );
    }


    @Test
    void tokenTest() throws AssertionGeneratorException {
        ClientCredentialsResponseDto c = pdndClient.createToken().block();
        log.info("Generated token -> " + c.getAccessToken());
        assertNotNull(c.getAccessToken());
        String[] chunks = c.getAccessToken().split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        log.info("Header: " + header);
        log.info("payload:" + payload);
    }
}