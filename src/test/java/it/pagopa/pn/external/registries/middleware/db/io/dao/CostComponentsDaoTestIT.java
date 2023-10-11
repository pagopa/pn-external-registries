package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.util.List;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class CostComponentsDaoTestIT {

    @Autowired
    private CostComponentsDao costComponentsDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    CostComponentsDao testDao;

    @BeforeEach
    void setup() {
        testDao = new CostComponentsDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig);
    }

    @Test
    void insertAndGet() {
        //Given
        CostComponentsEntity entity = newCostComponentsEntity();

        testDao.delete(entity.getPk(), entity.getSk()).block();
        testDao.insertOrUpdate(entity).block();

        //When
        CostComponentsEntity result = costComponentsDao.getItem(entity.getPk(), entity.getSk()).block();

        //Then
        Assertions.assertEquals(entity, result);
    }

    @Test
    void multipleInsertAndGetMultiple() {
        //Given
        CostComponentsEntity entity1 = newCostComponentsEntity();
        CostComponentsEntity entity2 = newCostComponentsEntity();
        entity2.setSk("sk2");

        testDao.delete(entity1.getPk(), entity1.getSk()).block();
        testDao.delete(entity2.getPk(), entity2.getSk()).block();
        testDao.insertOrUpdate(entity1).block();
        testDao.insertOrUpdate(entity2).block();

        //When
        List<CostComponentsEntity> result = costComponentsDao.getItems(entity1.getPk()).collectList().block();

        //Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(entity1, result.get(0));
        Assertions.assertEquals(entity2, result.get(1));
    }

    private CostComponentsEntity newCostComponentsEntity() {
        CostComponentsEntity costComponentsEntity = new CostComponentsEntity();
        costComponentsEntity.setPk("pk");
        costComponentsEntity.setSk("sk");
        costComponentsEntity.setBaseCost(1);
        costComponentsEntity.setSimpleRegisteredLetterCost(2);
        costComponentsEntity.setFirstAnalogCost(3);
        costComponentsEntity.setSecondAnalogCost(4);
        costComponentsEntity.setIsRefusedCancelled(false);
        return costComponentsEntity;
    }
}
