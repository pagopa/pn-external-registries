package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class CostUpdateResultDaoTestIT {
    @Autowired
    private CostUpdateResultDao costUpdateResultDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<CostUpdateResultEntity> testDao;

    @BeforeEach
    void setup() {
        testDao = new TestDao<>(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameCostUpdateResult(), CostUpdateResultEntity.class);
    }

    @Test
    void insertAndGet() {
        //Given
        CostUpdateResultEntity entity = newCostUpdateResultEntity();

        try {
            testDao.delete(entity.getPk(), entity.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }
        costUpdateResultDao.insertOrUpdate(entity).block();

        //When
        CostUpdateResultEntity result = null;
        try {
            result = testDao.get(entity.getPk(), entity.getSk());
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }

        //Clean
        try {
            testDao.delete(entity.getPk(), entity.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //Then
        // since the TTL has been set by the DAO, we force the value of the returned one to be the same as the entity,
        // after checking it is different from the original value
        Assertions.assertNotEquals(entity.getTtl(), result.getTtl());
        result.setTtl(entity.getTtl());
        Assertions.assertEquals(entity, result);
    }

    @Test
    void insertAndReinsertUpdating() {
        //Given
        CostUpdateResultEntity entity = newCostUpdateResultEntity();

        try {
            testDao.delete(entity.getPk(), entity.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }
        costUpdateResultDao.insertOrUpdate(entity).block();
        entity.setCommunicationResult("newCommunicationResult");
        costUpdateResultDao.insertOrUpdate(entity).block();

        //When
        CostUpdateResultEntity result = null;
        try {
            result = testDao.get(entity.getPk(), entity.getSk());
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }

        //Clean
        try {
            testDao.delete(entity.getPk(), entity.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //Then
        // since the TTL has been set by the DAO, we force the value of the returned one to be the same as the entity,
        // after checking it is different from the original value
        Assertions.assertNotEquals(entity.getTtl(), result.getTtl());
        result.setTtl(entity.getTtl());
        Assertions.assertEquals(entity, result);
    }

    private CostUpdateResultEntity newCostUpdateResultEntity() {
        var now = Instant.now();
        CostUpdateResultEntity entity = new CostUpdateResultEntity();
        entity.setPk("pk");
        entity.setSk("sk");
        entity.setTtl(0L); // ignored and replaced by the DAO
        // fill all the fields
        entity.setRequestId("requestId");
        entity.setFailedIuv("failedIuv");
        entity.setCommunicationResult("communicationResult");
        entity.setCommunicationResultGroup("communicationResultGroup");
        entity.setUpdateCostPhase("updateCostPhase");
        entity.setNotificationCost(100);
        entity.setIun("iun");
        entity.setEventTimestamp(Instant.now());
        entity.setEventStorageTimestamp(now.plusSeconds(1));
        entity.setCommunicationTimestamp(now.plusSeconds(5));
        entity.setJsonResponse("{object: 'value'}");
        return entity;
    }
}
