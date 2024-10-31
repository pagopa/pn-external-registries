package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.List;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class SenderConfigurationDaoTestIT {

    @Autowired
    private SenderConfigurationDao senderConfigurationDao;

    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @Test
    void insertSecondPaLangConfigurationAndGet() {

        senderConfigurationDao.createOrUpdateLang("CFG_testPaId", SenderConfigurationType.LANG, List.of("DE")).block();
        senderConfigurationDao.createOrUpdateLang("CFG_testPaId", SenderConfigurationType.LANG, List.of("FR")).block();

        LanguageDetailEntity result = senderConfigurationDao.getSenderConfiguration("CFG_testPaId", SenderConfigurationType.LANG).block();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(List.of("FR"), result.getValue().getAdditionalLangs());
        Assertions.assertNotEquals(result.getCreatedAt(), result.getUpdatedAt());
    }
}