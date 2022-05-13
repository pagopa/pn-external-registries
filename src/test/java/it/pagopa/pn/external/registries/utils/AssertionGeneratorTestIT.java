package it.pagopa.pn.external.registries.utils;

import it.pagopa.pn.external.registries.config.AccessTokenConfig;
import it.pagopa.pn.external.registries.config.ClockBeanConfig;
import it.pagopa.pn.external.registries.config.JwtConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import it.pagopa.pn.external.registries.exceptions.AssertionGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

@EnableConfigurationProperties
@SpringBootTest(classes = {
        AssertionGenerator.class, AwsConfigs.class, AwsServicesClientsConfig.class,
        PnExternalRegistriesConfig.class, ClockBeanConfig.class
}, properties = {
    "pn.external-registry.pdnd-m2m.jwt-cfg.issuer=43ce03cd-30ae-4e79-be48-dbb40207e3e1",
    "pn.external-registry.pdnd-m2m.jwt-cfg.subject=43ce03cd-30ae-4e79-be48-dbb40207e3e1",
    "pn.external-registry.pdnd-m2m.jwt-cfg.audience=test.interop.pagopa.it",
    "pn.external-registry.pdnd-m2m.jwt-cfg.kid=sM93ZfR7FmKtlW_rmHD_d6IwfjO7LwbC6MYTBLWFULE",
    "pn.external-registry.pdnd-m2m.jwt-cfg.client-assertion-ttl=600",
    "pn.external-registry.pdnd-m2m.jwt-cfg.keypair-alias=alias/pn-pdnd-authentication-token-key",
    "pn.external-registry.pdnd-m2m.client-assertion-type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
    "pn.external-registry.pdnd-m2m.grant-type=client_credentials",
    "pn.external-registry.pdnd-m2m.client-id=43ce03cd-30ae-4e79-be48-dbb40207e3e1",

    "aws.region-code=eu-central-1",
    "aws.profile-name=staging",
    "aws.endpoint-url",
})
@Slf4j
class AssertionGeneratorTestIT {

    @Autowired
    private AssertionGenerator assertionGenerator;

    @Autowired
    private PnExternalRegistriesConfig config;

    @Autowired
    private AwsConfigs awsConfig;

    @Test
    void generateClientAssertion() throws AssertionGeneratorException, ExecutionException, InterruptedException {
/*
        AccessTokenConfig pdndAccessTokenConfig = config.getAccessTokens()
                .get(PnExternalRegistriesConfig.PDND_M2M_TOKEN);

        JwtConfig pdndJwtCfg = pdndAccessTokenConfig.getJwtCfg();

        String result = assertionGenerator.generateClientAssertion(pdndJwtCfg).get();
        System.out.println( result );*/
    }
}
