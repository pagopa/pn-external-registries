package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ContextConfiguration( classes = {
        AssertionGenerator.class, AwsConfigs.class, AwsServicesClientsConfig.class,
        PnExternalRegistriesConfig.class
})
@Slf4j
class AssertionGeneratorTestIT {

    @Autowired
    private AssertionGenerator assertionGenerator;

    @Autowired
    private AwsConfigs awsConfigs;

    @Autowired
    private AwsServicesClientsConfig awsServicesClientsConfig;

    @Autowired
    private PnExternalRegistriesConfig config;

    @Test
    void generateClientAssertion() throws AssertionGeneratorException, ExecutionException, InterruptedException {
        log.info("AssertionGeneratorTest -> generateClientAssertion ... init ...");
        assertionGenerator.generateClientAssertion( config.getPdndM2m().getJwtCfg() ).get();
    }
}
