package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.timeout.ReadTimeoutException;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static it.pagopa.pn.external.registries.middleware.msclient.onetrust.OneTrustClient.PRIVACY_NOTICES_URL;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(properties = {
        "pn.external-registry.onetrust-base-url=http://localhost:9999",
        "pn.external-registry.onetrust-token=token-12345"
})
class OneTrustClientTest extends MockAWSObjectsTestConfig {

    private static ClientAndServer mockServer;

    @Autowired
    private OneTrustClient client;


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
                        .withPath(PRIVACY_NOTICES_URL.replace("{privacyNoticeId}", "374715bb-ce74-4e4e-bf85-4595bc485870"))
                        .withQueryStringParameter("date", LocalDate.now().plusDays(1).toString())
                        .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "token-12345"))
                .respond(response()
                        .withBody(oneTrustResponseInt())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        var expectedResponse = new ObjectMapper().readValue(oneTrustResult(), PrivacyNoticeOneTrustResult.class);
        Mono<PrivacyNoticeOneTrustResult> actualResponse = client.getPrivacyNoticeVersionByPrivacyNoticeId("374715bb-ce74-4e4e-bf85-4595bc485870");

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

        Mono<PrivacyNoticeOneTrustResult> actualResponse = client.getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6");
        StepVerifier.create(actualResponse)
                .expectError(WebClientResponseException.InternalServerError.class)
                .verify();

    }

    //testo il read timeout impostato da pn.external-registry.onetrust-read-timeout-millis o
    // PN_EXTERNAL_REGISTRY_ONETRUST_READ_TIMEOUT_MILLIS
    @Test
    void getPrivacyNoticeVersionByPrivacyNoticeIdKOReadTimeout() {


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath(PRIVACY_NOTICES_URL.replace("{privacyNoticeId}", "read-timeout"))
                        .withQueryStringParameter("date", LocalDate.now().plusDays(1).toString())
                        .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "token-12345"))
                .respond(response()
                        .withDelay(Delay.delay(TimeUnit.SECONDS, 5))
                        .withBody(oneTrustResponseInt())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        Mono<PrivacyNoticeOneTrustResult> actualResponse = client.getPrivacyNoticeVersionByPrivacyNoticeId("read-timeout");
        StepVerifier.create(actualResponse)
                .expectErrorMatches(throwable ->
                    throwable instanceof WebClientRequestException wex
                            && wex.getMostSpecificCause() instanceof ReadTimeoutException
                )
                .verify();

    }


    private String oneTrustResult() {
        return """
                {
                     "id": "P0da531e-8370-4373-8bd2-61ddc89e7fa8",
                     "createdDate": "2022-11-09T00:11:30.77Z",
                     "lastPublishedDate": "2022-11-15T07:23:18.347",
                     "organizationId": "d18cc1ca-2130-4edf-a1d6-f745a2e4fe12",
                     "responsibleUserId": "018cc1ca-2130-4edf-a1d6-f745a2e4fe19",
                     "version": {
                         "id": "z0da531e-8370-4373-8bd2-61ddc89e7fa6",
                         "name": "PN - Cittadini - ToS",
                         "publishedDate": "2022-11-15T07:23:18.347",
                         "status": "PUBLISHED",
                         "version": 9
                     }
                }
                """;
    }

    private String oneTrustResponseInt() {
        return """
                {
                  "versions": [
                    {
                      "id": "z0da531e-8370-4373-8bd2-61ddc89e7fa6",
                      "attachmentId": null,
                      "descriptionOfChanges": "update",
                      "publishedDate": "2022-11-15T07:23:18.347",
                      "createdDate": "2022-11-09T00:11:30.77Z",
                      "versionStatus": "PUBLISHED",
                      "policyContentType": "ONETRUST",
                      "sections": [
                        {
                          "name": "Introduzione",
                          "description": "",
                          "content": "Test",
                          "sectionType": "PLAIN",
                          "order": 1
                        },
                        {
                          "name": "1. Descrizione del servizio",
                          "description": "",
                          "content": "Test",
                          "sectionType": "PLAIN",
                          "order": 2
                        },
                        {
                          "name": "2. Identificazione e accesso alla Piattaforma",
                          "description": "",
                          "content": "Test",
                          "sectionType": "PLAIN",
                          "order": 3
                        },
                        {
                          "name": "3. Delega per l’accesso",
                          "description": "",
                          "content": "Test",
                          "sectionType": "PLAIN",
                          "order": 4
                        }
                      ],
                      "majorVersion": 9,
                      "minorVersion": 0
                    }
                  ],
                  "guid": "P0da531e-8370-4373-8bd2-61ddc89e7fa8",
                  "name": "PN - Cittadini - ToS",
                  "description": "Termini di utilizzo del Portale Cittadini di Piattaforma Notifiche",
                  "orgGroup": {
                    "id": "d18cc1ca-2130-4edf-a1d6-f745a2e4fe12",
                    "name": "PagoPA S.p.A."
                  },
                  "owners": [
                    {
                      "id": "019cc1ca-2130-5edf-a1d6-f745a2e4fe19",
                      "name": "Mario Rossi",
                      "email": "mario.rossi@email.it"
                    }
                  ],
                  "approvers": [
                    {
                      "id": "018cc1ca-2130-4edf-a1d6-f745a2e4fe19",
                      "name": "Mario Rossi",
                      "email": "mario.rossi@email.it"
                    }
                  ],
                  "effectiveDate": null,
                  "expirationDate": null,
                  "id": 24,
                  "defaultLanguageCode": "it"
                }
                """;
    }

}
