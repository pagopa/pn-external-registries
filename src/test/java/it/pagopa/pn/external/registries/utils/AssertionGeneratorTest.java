package it.pagopa.pn.external.registries.utils;

import it.pagopa.pn.external.registries.config.AccessTokenConfig;
import it.pagopa.pn.external.registries.config.ClockBeanConfig;
import it.pagopa.pn.external.registries.config.JwtConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.AssertionGeneratorException;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = {AssertionGenerator.class, ClockBeanConfig.class, PnExternalRegistriesConfig.class})
@TestPropertySource( properties = {
        "pn.external-registry.pdnd-m2m.jwt-cfg.issuer=43ce03cd-30ae-4e79-be48-dbb40207e3e1",
        "pn.external-registry.pdnd-m2m.jwt-cfg.subject=43ce03cd-30ae-4e79-be48-dbb40207e3e1",
        "pn.external-registry.pdnd-m2m.jwt-cfg.audience=test.interop.pagopa.it",
        "pn.external-registry.pdnd-m2m.jwt-cfg.kid=sM93ZfR7FmKtlW_rmHD_d6IwfjO7LwbC6MYTBLWFULE",
        "pn.external-registry.pdnd-m2m.jwt-cfg.client-assertion-ttl=600",
        "pn.external-registry.pdnd-m2m.jwt-cfg.keypair-alias=alias/pn-pdnd-authentication-token-key",
        "pn.external-registry.pdnd-m2m.client-assertion-type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
        "pn.external-registry.pdnd-m2m.grant-type=client_credentials",
        "pn.external-registry.pdnd-m2m.client-id=43ce03cd-30ae-4e79-be48-dbb40207e3e1",

        "aws.region-code=us-east-1",
        "aws.profile-name=default",
        "aws.endpoint-url=http://localhost:4566",
})
class AssertionGeneratorTest {


    @Autowired
    private AssertionGenerator assertionGenerator;

    @MockBean
    private PnExternalRegistriesConfig config;

    @Autowired
    private ClockBeanConfig clockBeanConfig;

    @MockBean
    private KmsAsyncClient kmsAsyncClient;

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public SqsNotificationPaidProducer sqsNotificationPaidProducer() {
            return Mockito.mock( SqsNotificationPaidProducer.class);
        }
    }

    @BeforeEach
    void setup() {
        Map<String,AccessTokenConfig> map = new HashMap<>();
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setClientAssertionTtl( 100 );
        AccessTokenConfig accessTokenConfig = new AccessTokenConfig();
        accessTokenConfig.setJwtCfg( jwtConfig );
        map.putIfAbsent( "pdnd", accessTokenConfig);
        Mockito.when( config.getAccessTokens() ).thenReturn( map );
    }


    @Test
    void generateClientAssertion() {
        //GIVEN
        AccessTokenConfig pdndAccessTokenConfig = config.getAccessTokens()
                .get(PnExternalRegistriesConfig.PDND_M2M_TOKEN);

        JwtConfig pdndJwtCfg = pdndAccessTokenConfig.getJwtCfg();

        byte[] signature = "abcd".getBytes(StandardCharsets.UTF_8);
        String base64signature = "YWJjZA";

        SignResponse signResponse = SignResponse.builder()
                .signature(SdkBytes.fromByteArray(signature)).build();

        Mockito.when(kmsAsyncClient.sign(Mockito.any(SignRequest.class))).thenReturn(CompletableFuture.completedFuture(signResponse));

        //WHEN
        String result = null;
        try {
            result = assertionGenerator.generateClientAssertion(pdndJwtCfg).get();
        } catch (Exception e) {
           fail(e);
        }

        //THEN
        assertNotNull(result);
        assertTrue(result.endsWith(base64signature));
    }

    @Test
    void generateClientAssertionFail() {
        //GIVEN
        AccessTokenConfig pdndAccessTokenConfig = config.getAccessTokens()
                .get(PnExternalRegistriesConfig.PDND_M2M_TOKEN);

        JwtConfig pdndJwtCfg = pdndAccessTokenConfig.getJwtCfg();

        byte[] signature = "abcd".getBytes(StandardCharsets.UTF_8);
        String base64signature = "YWJjZA";

        SignResponse signResponse = SignResponse.builder()
                .signature(SdkBytes.fromByteArray(signature)).build();

        Mockito.when(kmsAsyncClient.sign(Mockito.any(SignRequest.class))).thenThrow(new NullPointerException());

        //WHEN
        assertThrows(AssertionGeneratorException.class, () -> {
            assertionGenerator.generateClientAssertion(pdndJwtCfg).get();
        });
    }
}