package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.io.entities.IOMessagesEntity;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(LocalStackTestConfig.class)
class IOMessagesDaoTestIT {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private IOMessagesDao consentDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<IOMessagesEntity> testDao;

    @MockBean
    private SqsNotificationPaidProducer producer;


    @BeforeEach
    void setup() {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameIOMessages(), IOMessagesEntity.class);
    }

    @Test
    void save() {
        //Given
        IOMessagesEntity entity = newOptin();

        try {
            testDao.delete(entity.getPk(), null);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        consentDao.save(entity).block(d);

        //Then
        try {
            IOMessagesEntity elementFromDb = testDao.get(entity.getPk(), null);

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
    void get() {

        //Given
        IOMessagesEntity entity = newOptin();


        try {
            testDao.delete(entity.getPk(), null);
            consentDao.save(entity).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        IOMessagesEntity result = consentDao.get(entity.getHashedTaxId()).block(d);

        //Then

        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(entity, result);
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

    private IOMessagesEntity newOptin() {
        IOMessagesEntity res = new IOMessagesEntity("123123123");
        return res;
    }
}