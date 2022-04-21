package it.pagopa.pn.external.registries.middleware.microservice.common;

import it.pagopa.pn.external.registries.pdnd.service.AccessTokenCacheService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
class BaseClientTest {

    @Mock
    AccessTokenCacheService accessTokenCacheService;

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
    void getApiClient() {
        //GIVEN
        String token = "newtoken123456";
        String hello = "world";
        when(accessTokenCacheService.getToken(Mockito.anyString())).thenReturn(Mono.just(token));


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + token)
                        .withPath("/fake/helloworld/{hello}".replace("{hello}", hello)))
                .respond(response()
                        .withBody("hello " + hello)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //WHEN
        FakeApiClient fakeApiClient = new FakeApiClient(accessTokenCacheService);
        String resp = fakeApiClient.fakeHelloWorld(hello).bodyToMono(String.class).block(Duration.ofMillis(3000));

        //THEN
        assertEquals("hello " +  hello, resp);
    }

    @Test
    void getApiClientUnauthorizedAndRetrySuccess() {
        // fa 2 richieste, la prima con un token scaduto, che torna 401, e in automatico avviene il rinnovo con token valido
        //GIVEN
        String tokenExpired = "token123456_EXPIRED";
        String tokenValid = "token123456_VALID";
        String hello = "world";
        when(accessTokenCacheService.getToken(Mockito.anyString()))
                .thenReturn(Mono.just(tokenExpired))
                .thenReturn(Mono.just(tokenValid));


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + tokenExpired)
                        .withPath("/fake/helloworld/{hello}".replace("{hello}", hello)))
                .respond(response()
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withStatusCode(HttpStatus.UNAUTHORIZED.value()));

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + tokenValid)
                        .withPath("/fake/helloworld/{hello}".replace("{hello}", hello)))
                .respond(response()
                        .withBody("hello " + hello)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //WHEN
        FakeApiClient fakeApiClient = new FakeApiClient(accessTokenCacheService);
        String resp = fakeApiClient.fakeHelloWorld(hello).bodyToMono(String.class).block(Duration.ofMillis(3000));

        //THEN
        assertEquals("hello " +  hello, resp);
    }

    @Test
    void getApiClientUnauthorizedAndRetryFailed() {
        // fa 2 richieste, la prima con un token scaduto, che torna 401, e in automatico avviene il rinnovo con token che però non è valido
        // ci si aspetta un KO per Unauthorized
        //GIVEN
        String tokenExpired = "token123456_EXPIRED";
        String tokenValid = "token123456_VALID";
        String hello = "world";
        when(accessTokenCacheService.getToken(Mockito.anyString()))
                .thenReturn(Mono.just(tokenExpired));


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + tokenExpired)
                        .withPath("/fake/helloworld/{hello}".replace("{hello}", hello)))
                .respond(response()
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withStatusCode(HttpStatus.UNAUTHORIZED.value()));

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + tokenValid)
                        .withPath("/fake/helloworld/{hello}".replace("{hello}", hello)))
                .respond(response()
                        .withBody("hello " + hello)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //WHEN
        FakeApiClient fakeApiClient = new FakeApiClient(accessTokenCacheService);
        Mono<String> httpcall = fakeApiClient.fakeHelloWorld(hello).bodyToMono(String.class);
        Duration duration = Duration.ofMillis(3000);
        try {
            httpcall.block(duration);
            fail("no WebClientResponseException thrown");
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        } catch (Exception e) {
            fail("wrong exception thrown");
        }

        //THEN
    }


    private static class FakeApiClient extends BaseClient
    {
        public FakeApiClient(AccessTokenCacheService accessTokenCacheService){
            super(accessTokenCacheService, "M2M", "http://localhost:9999");
        }

        public WebClient.ResponseSpec fakeHelloWorld(String hello)
        {
            Object postBody = null;

            // create path and map variables
            final Map<String, Object> pathParams = new HashMap<>();
            pathParams.put("hello", hello);

            final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            final HttpHeaders headerParams = new HttpHeaders();
            final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
            final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

            final String[] localVarAccepts = {
                    "application/json"
            };
            final List<MediaType> localVarAccept = getApiClient().selectHeaderAccept(localVarAccepts);
            final String[] localVarContentTypes = {
                    "application/x-www-form-urlencoded"
            };
            final MediaType localVarContentType = getApiClient().selectHeaderContentType(localVarContentTypes);

            String[] localVarAuthNames = new String[] { "bearerAuth" };

            ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<>() {
            };
            return getApiClient().invokeAPI("/fake/helloworld/{hello}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        }
    }
}