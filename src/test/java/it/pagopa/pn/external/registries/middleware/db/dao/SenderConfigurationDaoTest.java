package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import it.pagopa.pn.external.registries.util.SenderConfigurationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

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

        senderConfigurationDao.putItem(entity).block();

        // When
        LanguageDetailEntity result = senderConfigurationDao.getSenderConfiguration(SenderConfigurationUtils.getPk("CFG-testPaId")).block();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(SenderConfigurationUtils.getPk("CFG-testPaId"), result.getPk());
        Assertions.assertEquals(entity.getValue(), result.getValue());
    }

    private LanguageDetailEntity newLanguageDetailEntity() {
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setPk(SenderConfigurationUtils.buildPk("testPaId"));
        entity.setSk("testSk");
        entity.setValue(Collections.singletonMap("langTest", "DE"));
        return entity;
    }
}