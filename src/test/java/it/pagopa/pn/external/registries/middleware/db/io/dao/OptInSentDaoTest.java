package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.io.entities.OptInSentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "aws.region-code=us-east-1",
        "aws.profile-name=${PN_AWS_PROFILE_NAME:default}",
        "aws.endpoint-url=http://localhost:4566"
})
@SpringBootTest
class OptInSentDaoTest {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private OptInSentDao consentDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<OptInSentEntity> testDao;

    @BeforeEach
    void setup() {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOptIn(), OptInSentEntity.class);
    }

    @Test
    void save() {
        //Given
        OptInSentEntity entity = newOptin();

        try {
            testDao.delete(entity.getPk(), null);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        consentDao.save(entity).block(d);

        //Then
        try {
            OptInSentEntity elementFromDb = testDao.get(entity.getPk(), null);

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
        OptInSentEntity entity = newOptin();


        try {
            testDao.delete(entity.getPk(), null);
            consentDao.save(entity).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        OptInSentEntity result = consentDao.get(entity.getHashedTaxId()).block(d);

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

    private OptInSentEntity newOptin() {
        OptInSentEntity res = new OptInSentEntity("123123123");
        return res;
    }
}