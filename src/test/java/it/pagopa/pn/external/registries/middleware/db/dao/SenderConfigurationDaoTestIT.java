package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Collections;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class SenderConfigurationDaoTestIT {

    @Autowired
    private SenderConfigurationDao senderConfigurationDao;

    @Test
    void getPaLangConfiguration() {
        LanguageDetailEntity entity = newLanguageDetailEntity();

        senderConfigurationDao.putItem(entity).block();
        LanguageDetailEntity result = senderConfigurationDao.getSenderConfiguration("testPaId", SenderConfigurationType.LANG).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals("CFG-testPaId", result.getHashKey());
        Assertions.assertEquals(entity.getValue(), result.getValue());
    }

    private LanguageDetailEntity newLanguageDetailEntity() {
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey(LanguageDetailEntity.buildPk("testPaId"));
        entity.setSortKey("LANG");
        LangConfig langConfig = new LangConfig();
        langConfig.setAdditionalLangs(Collections.singletonList("DE"));
        entity.setValue(langConfig);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}