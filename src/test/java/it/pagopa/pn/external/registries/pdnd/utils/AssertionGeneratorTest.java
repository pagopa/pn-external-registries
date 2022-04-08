package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class AssertionGeneratorTest {

    @Autowired
    PnExternalRegistriesConfig config;

    @Test
    void contextLoads() {
    }

    @Test
    void generateClientAssertion() {
        System.out.println("AssertionGeneratorTest -> generateClientAssertion ... init ...");
        AssertionGenerator assertionGenerator = new AssertionGenerator(config);
        String jtwToken= assertionGenerator.generateClientAssertion();
        System.out.printf("token: "+ jtwToken);

    }
}
