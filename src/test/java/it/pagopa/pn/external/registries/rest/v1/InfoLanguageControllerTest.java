package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.services.InfoLanguageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PACONFIGNOTFOUND;
import static it.pagopa.pn.external.registries.services.InfoLanguageService.ADDITIONAL_LANG_NOTFOUND;
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

        when(infoLanguageService.retrievePaAdditionalLang(paId)).thenReturn(Mono.just(expectedResponse));

        webTestClient.get()
                .uri(url, paId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    void getAdditionalLangNotFOund() {
        String url = "/ext-registry-private/pa/v1/additional-lang/{paId}";
        String paId = "testPaId";

        List<String> languages = new ArrayList<>();
        languages.add("DE");

        AdditionalLanguagesDto expectedResponse = new AdditionalLanguagesDto();
        expectedResponse.setPaId(paId);
        expectedResponse.setAdditionalLanguages(languages);

        when(infoLanguageService.retrievePaAdditionalLang(paId))
                .thenReturn(Mono.error(new PnInternalException(ADDITIONAL_LANG_NOTFOUND, 404, ERROR_CODE_EXTERNALREGISTRIES_PACONFIGNOTFOUND)));

        webTestClient.get()
                .uri(url, paId)
                .exchange()
                .expectStatus().isNotFound();
    }
}