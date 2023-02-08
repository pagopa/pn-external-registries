package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static it.pagopa.pn.external.registries.middleware.msclient.onetrust.OneTrustClient.PRIVACY_NOTICES_URL;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class OneTrustClientTest {

    private static ClientAndServer mockServer;
    private OneTrustClient client;


    @BeforeEach
    void setup() {
        PnExternalRegistriesConfig cfg = Mockito.mock(PnExternalRegistriesConfig.class);
        Mockito.when( cfg.getOnetrustBaseUrl() ).thenReturn( "http://localhost:9999" );
        Mockito.when( cfg.getOnetrustToken() ).thenReturn( "token-12345" );
        this.client = new OneTrustClient(cfg);
        this.client.init();
    }

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }


    @Test
    void getPrivacyNoticeVersionByPrivacyNoticeIdOK() throws JsonProcessingException {


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath(PRIVACY_NOTICES_URL.replace("{privacyNoticeId}", "q0da531e-8370-4373-8bd2-61ddc89e7fa6"))
                        .withQueryStringParameter("date", LocalDate.now().plusDays(1).toString())
                        .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "token-12345"))
                .respond(response()
                        .withBody(oneTrustResponse())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        var expectedResponse = new ObjectMapper().readValue(oneTrustResponse(), PrivacyNoticeOneTrustResponse.class);
        Mono<PrivacyNoticeOneTrustResponse> actualResponse = client.getPrivacyNoticeVersionByPrivacyNoticeId("q0da531e-8370-4373-8bd2-61ddc89e7fa6");

        StepVerifier.create(actualResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

    }

    @Test
    void getPrivacyNoticeVersionByPrivacyNoticeIdKO() {


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath(PRIVACY_NOTICES_URL.replace("{privacyNoticeId}", "z0da531e-8370-4373-8bd2-61ddc89e7fa6"))
                        .withQueryStringParameter("date", LocalDate.now().plusDays(1).toString())
                        .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "token-12345"))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(500));

        Mono<PrivacyNoticeOneTrustResponse> actualResponse = client.getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6");
        StepVerifier.create(actualResponse)
                .expectError(WebClientResponseException.InternalServerError.class)
                .verify();

    }


    private String oneTrustResponse() {
        return """
                {
                     "id": "z0da531e-8370-4373-8bd2-61ddc89e7fa6",
                     "createdDate": "2022-11-09T00:11:30.77",
                     "lastPublishedDate": "2022-11-15T07:23:18.347",
                     "organizationId": "018cc1ca-2130-4edf-a1d6-f745a2e4fe19",
                     "responsibleUserId": null,
                     "version": {
                         "id": "374715bb-ce74-4e4e-bf85-4595bc485870",
                         "name": "Prova - ToS",
                         "publishedDate": "2022-11-15T07:23:18.347",
                         "status": "ACTIVE",
                         "version": 1
                     }
                 }
                """;
    }

}
