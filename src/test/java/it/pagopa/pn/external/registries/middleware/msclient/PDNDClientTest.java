package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.AccessTokenConfig;
import it.pagopa.pn.external.registries.config.JwtConfig;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.TokenTypeDto;
import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.pdnd-server-url=http://localhost:9999",
        "pn.data-vault.tokenizer_api_key_pf=pf",
        "pn.data-vault.tokenizer_api_key_pg=pg"
})
class PDNDClientTest {



    @Autowired
    private PDNDClient client;

    @MockBean
    private AssertionGenerator assertionGenerator;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }



    @Test
    void testCreateToken() throws JsonProcessingException {
        //Given
        String token = "123456";
        AccessTokenConfig accessTokenCfg = new AccessTokenConfig();
        accessTokenCfg.setClientAssertionType("clientassertiontype");
        accessTokenCfg.setClientId("pagopa");
        accessTokenCfg.setGrantType("total");
        accessTokenCfg.setJwtCfg(new JwtConfig());
        ClientCredentialsResponseDto response = new ClientCredentialsResponseDto();
        response.setAccessToken(token);
        response.setExpiresIn(123);
        response.setTokenType(TokenTypeDto.BEARER);
        ObjectMapper mapper = new ObjectMapper();

        String respjson = mapper.writeValueAsString(response);
        when(assertionGenerator.generateClientAssertion(Mockito.any())).thenReturn(CompletableFuture.completedFuture(token));


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("POST")
                        .withPath("/as/token.oauth2"))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        ClientCredentialsResponseDto result = client.createToken(accessTokenCfg).block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
    }
}