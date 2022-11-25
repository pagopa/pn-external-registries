package it.pagopa.pn.external.registries.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonLoader;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import it.pagopa.pn.external.registries.middleware.db.io.entities.OptInSentEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.io.*;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
//@Import(LocalStackTestConfig.class)
@Slf4j
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "aws.region-code=us-east-1",
        "aws.profile-name=${PN_AWS_PROFILE_NAME:default}",
        "aws.endpoint-url=http://localhost:4566"
})
class OnboardInstitutionFulltextSearchHelperTest {

    @Autowired
    OnboardInstitutionsDao onboardInstitutionsDao;
    TestDao<OnboardInstitutionEntity> testDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    @Autowired
    OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper;

    @BeforeEach
    public void beforeall() throws IOException {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), OnboardInstitutionEntity.class);

        try {
            ClassPathResource res = new ClassPathResource("src/test/resources/testdata/ipas.json");
            File file = new File(res.getPath());
            JsonNode mySchema = JsonLoader.fromFile(file);
            ArrayNode records = (ArrayNode) mySchema.get("records");

            int i = 1;
            for (JsonNode d:
                 records) {
                OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
                entity.setPk(((ArrayNode)d).get(0).asText());
                entity.setDescription(((ArrayNode)d).get(2).asText());
                entity.setCreated(Instant.now());
                entity.setLastUpdate(Instant.now());
                entity.setTaxCode(((ArrayNode)d).get(3).asText());
                entity.setActive(true);
                entity.setStatus("ACTIVE");
                testDao.put(entity);
                if (i % 1000 == 0)
                    log.info("inserted " + i + "pas");
            }

        } catch (IOException e) {
           log.error(e.getMessage(), e);
           throw e;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void update() {

        Assertions.assertDoesNotThrow(() ->
            onboardInstitutionFulltextSearchHelper.update()
        );



    }

    @Test
    void fullTextSearch() {
    }
}