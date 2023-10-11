package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class CostComponentsDaoTestIT {

    @Autowired
    private CostComponentsDao testDao;

    @BeforeEach
    void setup() {
    }

    @Test
    void insertAndGet() {
        //Given
        CostComponentsEntity entity = newCostComponentsEntity();

        testDao.delete(entity.getPk(), entity.getSk()).block();
        testDao.insertOrUpdate(entity).block();

        //When
        CostComponentsEntity result = testDao.getItem(entity.getPk(), entity.getSk()).block();

        //Clean
        testDao.delete(entity.getPk(), entity.getSk()).block();

        //Then
        Assertions.assertEquals(entity, result);
    }

    @Test
    void insertAndReinsertUpdating() {
        //Given
        CostComponentsEntity entity = newCostComponentsEntity();

        testDao.delete(entity.getPk(), entity.getSk()).block();
        testDao.insertOrUpdate(entity).block();
        // little change before reinserting
        entity.setBaseCost(2);
        testDao.insertOrUpdate(entity).block();

        //When
        CostComponentsEntity result = testDao.getItem(entity.getPk(), entity.getSk()).block();

        //Clean
        testDao.delete(entity.getPk(), entity.getSk()).block();

        //Then
        Assertions.assertEquals(entity, result); // the second insertion should have overwritten the first one
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
        List<CostComponentsEntity> result = testDao.getItems(entity1.getPk()).collectList().block();

        //Clean
        testDao.delete(entity1.getPk(), entity1.getSk()).block();
        testDao.delete(entity2.getPk(), entity2.getSk()).block();

        //Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(entity1, result.get(0));
        Assertions.assertEquals(entity2, result.get(1));
    }

    @Test
    void multipleInsertWithDifferentPk() {
        //Given
        CostComponentsEntity entity1 = newCostComponentsEntity();
        CostComponentsEntity entity2 = newCostComponentsEntity();
        entity2.setPk("pk2");

        testDao.delete(entity1.getPk(), entity1.getSk()).block();
        testDao.delete(entity2.getPk(), entity2.getSk()).block();
        testDao.insertOrUpdate(entity1).block();
        testDao.insertOrUpdate(entity2).block();

        //When
        List<CostComponentsEntity> result1 = testDao.getItems(entity1.getPk()).collectList().block();
        List<CostComponentsEntity> result2 = testDao.getItems(entity2.getPk()).collectList().block();

        //Clean
        testDao.delete(entity1.getPk(), entity1.getSk()).block();
        testDao.delete(entity2.getPk(), entity2.getSk()).block();

        //Then
        Assertions.assertNotNull(result1);
        Assertions.assertEquals(1, result1.size());
        Assertions.assertEquals(entity1, result1.get(0));
        Assertions.assertNotNull(result2);
        Assertions.assertEquals(1, result2.size());
        Assertions.assertEquals(entity2, result2.get(0));
    }

    @Test
    void updateAndCheckNullsUnchanged() {
        //Given
        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setBaseCost(10);
        // set all other values to 0 and false
        entity.setSimpleRegisteredLetterCost(0);
        entity.setFirstAnalogCost(0);
        entity.setSecondAnalogCost(0);
        entity.setIsRefusedCancelled(true);

        testDao.delete(entity.getPk(), entity.getSk()).block();
        testDao.insertOrUpdate(entity).block();

        //When
        // set all parameters to null, except for firstAnalogCost
        entity.setBaseCost(null);
        entity.setSimpleRegisteredLetterCost(null);
        entity.setSecondAnalogCost(null);
        entity.setIsRefusedCancelled(null);
        entity.setFirstAnalogCost(12);
        // update the entity
        testDao.updateNotNull(entity).block();
        CostComponentsEntity result = testDao.getItem(entity.getPk(), entity.getSk()).block();

        //Clean
        testDao.delete(entity.getPk(), entity.getSk()).block();

        //Then all the other values should be unchanged and the set one must be present
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.getBaseCost());
        Assertions.assertEquals(0, result.getSimpleRegisteredLetterCost());
        Assertions.assertEquals(0, result.getSecondAnalogCost());
        Assertions.assertTrue(result.getIsRefusedCancelled());
        Assertions.assertEquals(12, result.getFirstAnalogCost());
    }

    @Test
    void updateWithoutInsertionFirst() {
        //Given
        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setBaseCost(10);
        // set all other values to 0 and false
        entity.setSimpleRegisteredLetterCost(0);
        entity.setFirstAnalogCost(0);
        entity.setSecondAnalogCost(0);
        entity.setIsRefusedCancelled(false);

        testDao.delete(entity.getPk(), entity.getSk()).block();

        //When
        // set all parameters to null, except for firstAnalogCost
        entity.setBaseCost(null);
        entity.setSimpleRegisteredLetterCost(null);
        entity.setSecondAnalogCost(null);
        entity.setIsRefusedCancelled(null);
        entity.setFirstAnalogCost(12);
        // update the entity
        testDao.updateNotNull(entity).block();
        CostComponentsEntity result = testDao.getItem(entity.getPk(), entity.getSk()).block();

        //Clean
        testDao.delete(entity.getPk(), entity.getSk()).block();

        //Then all the other values should be null and the set one must be present
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getBaseCost());
        Assertions.assertNull(result.getSimpleRegisteredLetterCost());
        Assertions.assertNull(result.getSecondAnalogCost());
        Assertions.assertNull(result.getIsRefusedCancelled());
        Assertions.assertEquals(12, result.getFirstAnalogCost());
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
