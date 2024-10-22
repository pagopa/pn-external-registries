package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import it.pagopa.pn.external.registries.util.SenderConfigurationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
        String paId = "testPaId";
        List<String> languages = new ArrayList<>();
        languages.add("DE");
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setPk(SenderConfigurationUtils.buildPk(paId));
        entity.setValue(Map.of("langTest", "DE"));

        when(senderConfigurationDao.getSenderConfiguration(paId)).thenReturn(Mono.just(entity));

        // When
        AdditionalLanguagesDto response = infoLanguageService.get(paId).block();

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(paId, response.getPaId(), "PaId should match");
        assertEquals(languages, response.getAdditionalLanguages(), "Languages should match");
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

}