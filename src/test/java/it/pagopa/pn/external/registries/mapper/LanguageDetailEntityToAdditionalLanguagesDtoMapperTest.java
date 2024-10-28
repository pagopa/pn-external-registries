package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LanguageDetailEntityToAdditionalLanguagesDtoMapperTest {

    @Test
    void toDto() {
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey("CFG-testPaId");
        LangConfig languagesMap = new LangConfig();
        languagesMap.setAdditionalLangs(Collections.singletonList("DE"));
        entity.setValue(languagesMap);

        AdditionalLanguagesDto dto = LanguageDetailEntityToAdditionalLanguagesDtoMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("testPaId", dto.getPaId());
        assertEquals(Collections.singletonList("DE"), dto.getAdditionalLanguages());
    }
}