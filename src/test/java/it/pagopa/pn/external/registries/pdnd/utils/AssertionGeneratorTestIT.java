package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Slf4j
class AssertionGeneratorTestIT {

    @Autowired
    AssertionGenerator assertionGenerator;

    @Autowired
    AwsConfigs awsConfigs;

    @Autowired
    AwsServicesClientsConfig awsServicesClientsConfig;

    @Autowired
    PnExternalRegistriesConfig config;

    @Test
    void generateClientAssertion() {
        System.out.println("AssertionGeneratorTest -> generateClientAssertion ... init ...");

        try {
            String jtwToken = assertionGenerator.generateClientAssertion();
            assertNotNull(jtwToken);
        }catch(Exception e){

        }
    }
}
