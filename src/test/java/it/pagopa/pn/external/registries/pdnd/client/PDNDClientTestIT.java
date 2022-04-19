package it.pagopa.pn.external.registries.pdnd.client;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import it.pagopa.pn.external.registries.config.aws.AwsServicesClientsConfig;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.ClientCredentialsResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class PDNDClientTestIT {

    @Autowired
    PDNDClient pdndClient;
    @Autowired
    PnExternalRegistriesConfig config;

    @Autowired
    AwsConfigs  awsConfigs;

    @Autowired
    AwsServicesClientsConfig awsServicesClientsConfig;



    @Test
    public void tokenTest()  {
        try {
            ClientCredentialsResponseDto c = pdndClient.createToken().block();
            log.info("Generated token -> " + c.getAccessToken());
            assertNotNull(c.getAccessToken());
            String[] chunks = c.getAccessToken().split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));
            log.info("Header: " + header);
            log.info("payload:" + payload);
        }catch(Exception e)
        {

        }
    }
}