package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
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
class SenderConfigurationDaoTest {

    @Autowired
    private SenderConfigurationDao senderConfigurationDao;

    @Test
    void Get_Test() {
        // Given
        LanguageDetailEntity entity = newLanguageDetailEntity();

        senderConfigurationDao.createOrUpdateLang(entity.getHashKey(), entity.getValue().getAdditionalLangs()).block();

        // When
        LanguageDetailEntity result = senderConfigurationDao.getSenderConfiguration(entity.getHashKey()).block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(entity.getHashKey(), result.getHashKey());
        Assertions.assertEquals(entity.getValue(), result.getValue());
    }

    @Test
    void insertAndReinsertUpdating() {
        // Given
        LanguageDetailEntity entity = newLanguageDetailEntity();
        LangConfig langConfig = new LangConfig();
        langConfig.getAdditionalLangs().add("DE");

        senderConfigurationDao.createOrUpdateLang(entity.getHashKey(), entity.getValue().getAdditionalLangs()).block();
        entity.setValue(langConfig);
        senderConfigurationDao.createOrUpdateLang(entity.getHashKey(), entity.getValue().getAdditionalLangs()).block();

        // When
        LanguageDetailEntity result = senderConfigurationDao.getSenderConfiguration(entity.getHashKey()).block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(entity.getHashKey(), result.getHashKey());
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