package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class OnboardInstitutionDaoTestIT {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private OnboardInstitutionsDao consentDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<OnboardInstitutionEntity> testDao;

    @MockBean
    private SqsNotificationPaidProducer producer;


    @BeforeEach
    void setup() {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), OnboardInstitutionEntity.class);
    }

    @Test
    void get() {
        //Given
        OnboardInstitutionEntity entity = newOnboard(true, false);

        try {
            testDao.delete(entity.getPk(), null);
            testDao.put(entity);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        consentDao.get(entity.getPk()).block(d);

        //Then
        try {
            OnboardInstitutionEntity elementFromDb = testDao.get(entity.getPk(), null);

            Assertions.assertNotNull(elementFromDb);
            Assertions.assertEquals(entity, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(entity.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNewer() {

        //Given
        OnboardInstitutionEntity entity = newOnboard(true, false);


        try {
            testDao.delete(entity.getPk(), null);
            testDao.put(entity);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        List<OnboardInstitutionEntity> result = consentDao.getNewer(null).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(entity, result.get(0));
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                testDao.delete(entity.getPk(), null);

            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNewerOfInstant() {

        //Given
        OnboardInstitutionEntity entity = newOnboard(true, false);
        OnboardInstitutionEntity entity1 = newOnboard(true, false);
        entity1.setPk(entity1.getPk() + "1");
        entity1.setLastUpdate(entity1.getLastUpdate().plusMillis(1000));


        try {
            testDao.delete(entity.getPk(), null);
            testDao.put(entity);
            testDao.put(entity1);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        List<OnboardInstitutionEntity> result = consentDao.getNewer(entity.getLastUpdate()).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals(entity1, result.get(0));
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                testDao.delete(entity.getPk(), null);
                testDao.delete(entity1.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    @Test
    void getAooUO() {
        //Given
        OnboardInstitutionEntity entity = newOnboard(false, false);


        try {
            testDao.delete(entity.getPk(), null);
            testDao.put(entity);
        } catch (Exception e) {
            System.out.println("Problem to insert");
        }

        //When
        List<OnboardInstitutionEntity> result = consentDao.getNewer(null).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(result);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                testDao.delete(entity.getPk(), null);

            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    @Test
    void filterOutRootIds() {
        //Given
        OnboardInstitutionEntity entityNotRoot = newOnboard(false, false);
        OnboardInstitutionEntity entityRoot = newOnboard(true, true);

        try {
            testDao.delete(entityNotRoot.getPk(), null);
            testDao.delete(entityRoot.getPk(), null);
            testDao.put(entityNotRoot);
            testDao.put(entityRoot);
        } catch (Exception e) {
            System.out.println("Problem to insert");
        }

        //When
        List<OnboardInstitutionEntity> result = consentDao.filterOutRootIds(List.of(entityNotRoot.getInstitutionId(),entityRoot.getInstitutionId())).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(result.get(0).getPk(), entityNotRoot.getInstitutionId());
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                testDao.delete(entityRoot.getPk(), null);
                testDao.delete(entityNotRoot.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    private OnboardInstitutionEntity newOnboard(boolean rootPa, boolean secondOnboard) {
        OnboardInstitutionEntity res = new OnboardInstitutionEntity();
        res.setPk(secondOnboard? "11111" : "22222" );
        res.setRootId(rootPa ? res.getPk() : "33333");
        res.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        res.setOnlyRootStatus(rootPa ? OnboardInstitutionEntity.STATUS_ACTIVE : null);
        res.setDescription("comune di milano");
        res.setTaxCode("123456");
        res.setCreated(Instant.EPOCH.plusMillis(1000));
        res.setLastUpdate(Instant.EPOCH.plusMillis(1000));
        return res;
    }

}