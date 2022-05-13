package it.pagopa.pn.external.registries.middleware.wsclient;

import it.pagopa.pn.external.registries.config.ClockBeanConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.PDNDClient;
import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import it.pagopa.pn.external.registries.exceptions.AssertionGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableConfigurationProperties
@SpringBootTest(classes = {
        AssertionGenerator.class, AwsConfigs.class, AwsServicesClientsConfig.class,
        PnExternalRegistriesConfig.class, ClockBeanConfig.class, PDNDClient.class
}, properties = {
        "pn.external-registry.access-tokens.anpr.jwt-cfg.issuer=10ecec03-7c15-4c4e-a482-a16a99d9b63b",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.subject=10ecec03-7c15-4c4e-a482-a16a99d9b63b",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.audience=test.interop.pagopa.it",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.kid=H9M2FiPLrBew5-L7pZjwPTVbBcpP8Iuqe0feaBfrnCc",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.client-assertion-ttl=600",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.purpose-id=fa2aa359-ab78-45b2-aa55-977d8ddb7d2b",
        "pn.external-registry.access-tokens.anpr.jwt-cfg.keypair-alias=alias/pn-PdndAnpr-authentication-token-key",
        "pn.external-registry.access-tokens.anpr.client-assertion-type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
        "pn.external-registry.access-tokens.anpr.grant-type=client_credentials",
        "pn.external-registry.access-tokens.anpr.client-id=10ecec03-7c15-4c4e-a482-a16a99d9b63b",


        "pn.external-registry.pdnd-server-url=https://uat.gateway.test.pdnd-interop.pagopa.it/api-gateway/0.1",

        "aws.region-code=eu-central-1",
        "aws.profile-name=staging",
        "aws.endpoint-url",
})
@Slf4j
class PDNDClientTestIT {

    @Autowired
    private PDNDClient pdndClient;

    @Test
    void tokenTest() throws AssertionGeneratorException {
        /*String purposeId = "anpr"; // PnExternalRegistriesConfig.PDND_M2M_TOKEN;

        ClientCredentialsResponseDto c = pdndClient.createToken( purposeId ).block();

        String accessToken = c.getAccessToken();

        log.info("Generated accessToken={}", accessToken );
        assertNotNull( accessToken );

        String[] chunks = c.getAccessToken().split("\\.");
        assertEquals( 3, chunks.length );


        Base64.Decoder decoder = Base64.getUrlDecoder();

        log.info("Header: {}", decodeChunk( chunks[0] ));
        log.info("Payload: {}", decodeChunk( chunks[1] ));*/
    }

    private String decodeChunk( String chunk ) {
        byte[] bytes = Base64Utils.decodeFromUrlSafeString( chunk );
        return new String( bytes, StandardCharsets.UTF_8 );
    }
}