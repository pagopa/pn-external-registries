package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LanguageDetailEntityToAdditionalLanguagesDtoMapperTest {

    @Test
    void toDto() {
        // Given
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setPk("CONFIG_testPaId");
        Map<String, String> languagesMap = new HashMap<>();
        languagesMap.put("langTest", "DE");
        entity.setValue(languagesMap);

        // When
        AdditionalLanguagesDto dto = LanguageDetailEntityToAdditionalLanguagesDtoMapper.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals("testPaId", dto.getPaId());
        assertEquals(Collections.singletonList("DE"), dto.getAdditionalLanguages());
    }

    @Test
    void createListFromMap() {
        // Given
        Map<String, String> languagesMap = new HashMap<>();
        languagesMap.put("lang1", "DE");
        languagesMap.put("lang2", "SL");

        // When
        var languagesList = LanguageDetailEntityToAdditionalLanguagesDtoMapper.createListFromMap(languagesMap);

        // Then
        assertNotNull(languagesList);
        assertEquals(2, languagesList.size());
        assertTrue(languagesList.contains("DE"));
        assertTrue(languagesList.contains("SL"));
    }
}