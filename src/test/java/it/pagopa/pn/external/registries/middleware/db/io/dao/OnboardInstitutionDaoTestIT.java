package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

    @Test
    void getNewerChildren() {
        // GIVEN
        OnboardInstitutionEntity root = newFatherOrChildOnboard("root", "root");
        OnboardInstitutionEntity child1 = newFatherOrChildOnboard("child1", "root");
        OnboardInstitutionEntity child2 = newFatherOrChildOnboard("child2", "child1");
        OnboardInstitutionEntity child3 = newFatherOrChildOnboard("child3", "child2");

        try {
            // Pulisce i dati
            testDao.delete(root.getPk(), null);
            testDao.delete(child1.getPk(), null);
            testDao.delete(child2.getPk(), null);
            testDao.delete(child3.getPk(), null);

            // Inserisce i dati
            testDao.put(root);
            testDao.put(child1);
            testDao.put(child2);
            testDao.put(child3);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // WHEN
        List<OnboardInstitutionEntity> result = consentDao.getNewerChildren(Instant.now().minusSeconds(3600)).collectList().block(d);

        // THEN
        try {
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(3, result.size());
            Assertions.assertTrue(result.contains(child1));
            Assertions.assertTrue(result.contains(child2));
            Assertions.assertTrue(result.contains(child3));
        } finally {
            try {
                testDao.delete(root.getPk(), null);
                testDao.delete(child1.getPk(), null);
                testDao.delete(child2.getPk(), null);
                testDao.delete(child3.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNewerChildrenEmptyResult() {
        // GIVEN
        OnboardInstitutionEntity padre = newFatherOrChildOnboard("root", "root");

        try {
            testDao.delete(padre.getPk(), null);
            testDao.put(padre);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // WHEN
        List<OnboardInstitutionEntity> result = consentDao.getNewerChildren(Instant.EPOCH).collectList().block(d);

        // THEN
        try {
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isEmpty());
        } finally {
            try {
                testDao.delete(padre.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNewerChildrenWithRecursiveFetch() {
        // GIVEN
        OnboardInstitutionEntity padre = newFatherOrChildOnboard("root", "root"); // Padre
        OnboardInstitutionEntity figlio1 = newFatherOrChildOnboard("child1", "root"); // Figlio diretto
        OnboardInstitutionEntity figlio2 = newFatherOrChildOnboard("child2", "child1"); // Figlio del figlio
        OnboardInstitutionEntity figlio3 = newFatherOrChildOnboard("child3", "child2"); // Figlio del figlio del figlio

        try {
            testDao.delete(padre.getPk(), null);
            testDao.delete(figlio1.getPk(), null);
            testDao.delete(figlio2.getPk(), null);
            testDao.delete(figlio3.getPk(), null);
            testDao.put(padre);
            testDao.put(figlio1);
            testDao.put(figlio2);
            testDao.put(figlio3);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // WHEN
        List<OnboardInstitutionEntity> result = consentDao.getNewerChildren(Instant.EPOCH).collectList().block(d);

        // THEN
        try {
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(3, result.size());
            Assertions.assertTrue(result.contains(figlio1));
            Assertions.assertTrue(result.contains(figlio2));
            Assertions.assertTrue(result.contains(figlio3));
        } finally {
            try {
                testDao.delete(padre.getPk(), null);
                testDao.delete(figlio1.getPk(), null);
                testDao.delete(figlio2.getPk(), null);
                testDao.delete(figlio3.getPk(), null);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNewerChildrenWithInstantNull() {
        // GIVEN
        OnboardInstitutionEntity padre = newFatherOrChildOnboard("root", "root"); // Padre principale
        OnboardInstitutionEntity figlio1 = newFatherOrChildOnboard("child1", "root"); // Figlio diretto 1
        OnboardInstitutionEntity figlio2 = newFatherOrChildOnboard("child2", "root"); // Figlio diretto 2
        OnboardInstitutionEntity figlio3 = newFatherOrChildOnboard("child3", "root"); // Figlio diretto 3

        try {
            testDao.delete(padre.getPk(), null);
            testDao.delete(figlio1.getPk(), null);
            testDao.delete(figlio2.getPk(), null);
            testDao.delete(figlio3.getPk(), null);
            testDao.put(padre);
            testDao.put(figlio1);
            testDao.put(figlio2);
            testDao.put(figlio3);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // WHEN
        List<OnboardInstitutionEntity> result = consentDao.getNewerChildren(null).collectList().block(d);

        // THEN
        try {
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(3, result.size());
            Assertions.assertTrue(result.contains(figlio1));
            Assertions.assertTrue(result.contains(figlio2));
            Assertions.assertTrue(result.contains(figlio3));
        } finally {
            try {
                testDao.delete(padre.getPk(), null);
                testDao.delete(figlio1.getPk(), null);
                testDao.delete(figlio2.getPk(), null);
                testDao.delete(figlio3.getPk(), null);
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

    private OnboardInstitutionEntity newFatherOrChildOnboard(String pk, String rootId) {
        OnboardInstitutionEntity res = new OnboardInstitutionEntity();
        res.setPk(pk);
        res.setRootId(rootId);
        res.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        res.setOnlyRootStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        res.setDescription("comune di milano");
        res.setTaxCode("123456");
        res.setCreated(Instant.EPOCH.plusMillis(1000));
        res.setLastUpdate(Instant.now());
        return res;
    }
}