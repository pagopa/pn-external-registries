package it.pagopa.pn.external.registries.pdnd.client;

import it.pagopa.pn.external.registries.PnExternalRegistriesApplication;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.ClientCredentialsResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class PnExternalRegistryApplicationTest {

    @Autowired
    PDNDClient pdndClient;
    @Autowired
    PnExternalRegistriesConfig config;

    @Test
    void contextLoads() {
    }

    @Test
    public void main() {
        PnExternalRegistriesApplication.main(new String[] {});
    }

    @Test
    public void tokenTest()
    {

        ClientCredentialsResponseDto c = pdndClient.createToken().block();
        assertNotNull(c.getAccessToken());
    }
}