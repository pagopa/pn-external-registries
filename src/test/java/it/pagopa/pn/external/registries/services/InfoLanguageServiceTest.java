package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InfoLanguageServiceTest {

    private InfoLanguageService infoLanguageService;

    @Mock
    private SenderConfigurationDao senderConfigurationDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        infoLanguageService = new InfoLanguageService(senderConfigurationDao);
    }

    @Test
    void testGetAdditionalLang_200_OK() {
        // Given
        List<String> langsList = new ArrayList<>();
        langsList.add("DE");

        LangConfig languages = new LangConfig();
        languages.setAdditionalLangs(langsList);
        String paId = "testPaId";
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey(paId);
        entity.setValue(languages);

        when(senderConfigurationDao.getSenderConfiguration(paId)).thenReturn(Mono.just(entity));

        // When
        AdditionalLanguagesDto response = infoLanguageService.get(paId).block();

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(paId, response.getPaId(), "PaId should match");
        assertEquals(langsList, response.getAdditionalLanguages(), "Languages should match");
    }

    @Test
    void testGetAdditionalLang_EmptyPaId() {
        // Given
        String paId = "";

        // When
        Mono<AdditionalLanguagesDto> response = infoLanguageService.get(paId);

        // Then
        response
                .doOnError(error -> {
                    assertTrue(error instanceof IllegalArgumentException, "Error should be IllegalArgumentException");
                    assertEquals("paId is empty", error.getMessage(), "Error message should match");
                })
                .subscribe();
    }

    @Test
    void testCreateOrUpdateLang_200_OK() {
        // Given

        List<String> langsList = new ArrayList<>();
        langsList.add("DE");

        LangConfig languages = new LangConfig();
        languages.setAdditionalLangs(langsList);

        String paId = "testPaId";
        AdditionalLanguagesDto request = new AdditionalLanguagesDto();
        request.setPaId(paId);
        request.setAdditionalLanguages(langsList);


        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey(paId);
        entity.setValue(languages);

        when(senderConfigurationDao.createOrUpdateLang(any(), any())).thenReturn(Mono.just(entity));

        // When
        AdditionalLanguagesDto response = infoLanguageService.createOrUpdateLang(request).block();

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(request.getPaId(), response.getPaId(), "PaId should match");
        assertEquals(langsList, response.getAdditionalLanguages(), "Languages should match");
    }

}