package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.services.InfoLanguageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {InfoLanguageController.class})
class InfoLanguageControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InfoLanguageService infoLanguageService;

    @Test
    void getAdditionalLang() throws JsonProcessingException {
        String url = "/ext-registry-private/pa/v1/additional-lang/{paId}";
        String paId = "testPaId";

        List<String> languages = new ArrayList<>();
        languages.add("DE");

        AdditionalLanguagesDto expectedResponse = new AdditionalLanguagesDto();
        expectedResponse.setPaId(paId);
        expectedResponse.setAdditionalLanguages(languages);

        when(infoLanguageService.get(paId)).thenReturn(Mono.just(expectedResponse));

        webTestClient.get()
                .uri(url, paId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    void putAdditionalLang() throws JsonProcessingException {
        // Given
        String url = "/ext-registry-private/pa/v1/additional-lang";

        List<String> languages = new ArrayList<>();
        languages.add("DE");

        AdditionalLanguagesDto request = new AdditionalLanguagesDto();
        request.setPaId("testPaId");
        request.setAdditionalLanguages(languages);

        AdditionalLanguagesDto expectedResponse = new AdditionalLanguagesDto();
        expectedResponse.setPaId("testPaId");
        expectedResponse.setAdditionalLanguages(languages);

        when(infoLanguageService.createOrUpdateLang(any())).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.put()
                .uri(url)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(expectedResponse));
    }

}