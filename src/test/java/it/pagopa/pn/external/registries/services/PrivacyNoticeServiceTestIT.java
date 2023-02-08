package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.exceptions.PnPrivacyNoticeNotFound;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.onetrust.OneTrustClient;
import it.pagopa.pn.external.registries.middleware.msclient.onetrust.PrivacyNoticeOneTrustResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class PrivacyNoticeServiceTestIT {

    @MockBean
    private OneTrustClient oneTrustClient;

    @Autowired
    private PrivacyNoticeService privacyNoticeService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @AfterEach
    public void destroyCache() {
        privacyNoticeService.getPrivacyNoticeCache().clear();
    }



    @Test
    void getPrivacyNoticeVersionWithoutCacheCall() throws JsonProcessingException {

        assertThat(privacyNoticeService.getPrivacyNoticeCache()).isEmpty();

        Mockito.when(oneTrustClient.getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6"))
                .thenReturn(Mono.just(objectMapper.readValue(oneTrustResponse(), PrivacyNoticeOneTrustResponse.class)));

        var expectedResponse = new PrivacyNoticeVersionResponseDto().version(1); //recuperato da one trust

        Mono<PrivacyNoticeVersionResponseDto> actualResponse = privacyNoticeService.getPrivacyNoticeVersion("TOS", "PF");

        StepVerifier.create(actualResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(oneTrustClient, Mockito.times(1))
                .getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6");

        assertThat(privacyNoticeService.getPrivacyNoticeCache())
                .hasSize(1)
                .containsKey("z0da531e-8370-4373-8bd2-61ddc89e7fa6");

    }

    @Test
    void getPrivacyNoticeVersionWithCacheCall() {

        privacyNoticeService.getPrivacyNoticeCache().put("z0da531e-8370-4373-8bd2-61ddc89e7fa6", 1);

        assertThat(privacyNoticeService.getPrivacyNoticeCache())
                .hasSize(1)
                .containsKey("z0da531e-8370-4373-8bd2-61ddc89e7fa6");

        Mockito.when(oneTrustClient.getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6"))
                .thenReturn(Mono.error(WebClientResponseException.create(500, "Error", new HttpHeaders(), null, null)));

        var expectedResponse = new PrivacyNoticeVersionResponseDto().version(1); //recuperato da cache

        Mono<PrivacyNoticeVersionResponseDto> actualResponse = privacyNoticeService.getPrivacyNoticeVersion("TOS", "PF");

        StepVerifier.create(actualResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

        Mockito.verify(oneTrustClient, Mockito.times(1))
                .getPrivacyNoticeVersionByPrivacyNoticeId("z0da531e-8370-4373-8bd2-61ddc89e7fa6");

    }

    @Test
    void getPrivacyNoticeVersionWithPnPrivacyNoticeNotFound() {


        Mono<PrivacyNoticeVersionResponseDto> actualResponse = privacyNoticeService.getPrivacyNoticeVersion("TOS", "PG");

        StepVerifier.create(actualResponse)
                .expectError(PnPrivacyNoticeNotFound.class)
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
