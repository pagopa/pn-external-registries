package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LanguageDetailEntityToAdditionalLanguagesDtoMapperTest {

    @Test
    void toDto() {
        // Given
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey("testPaId");
        List<String> languagesList = new ArrayList<>();
        languagesList.add("DE");
        LangConfig langConfig = new LangConfig();
        langConfig.setAdditionalLangs(languagesList);
        entity.setValue(langConfig);

        // When
        AdditionalLanguagesDto dto = LanguageDetailEntityToAdditionalLanguagesDtoMapper.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals("testPaId", dto.getPaId());
        assertEquals(Collections.singletonList("DE"), dto.getAdditionalLanguages());
    }

}