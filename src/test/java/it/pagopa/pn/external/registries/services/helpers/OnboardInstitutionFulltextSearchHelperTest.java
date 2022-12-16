package it.pagopa.pn.external.registries.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonLoader;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(LocalStackTestConfig.class)
@Slf4j
@ActiveProfiles("test")
class OnboardInstitutionFulltextSearchHelperTest {

    Duration d = Duration.ofMillis(3000);

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
    public void beforeeach() throws IOException {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), OnboardInstitutionEntity.class);

        try {
            ClassPathResource res = new ClassPathResource("src/test/resources/testdata/ipas.json");
            File file = new File(res.getPath());
            JsonNode mySchema = JsonLoader.fromFile(file);
            ArrayNode records = (ArrayNode) mySchema.get("records");

            OnboardInstitutionEntity prev = testDao.get(((ArrayNode)records.get(0)).get(0).asText(), null);
            if (prev == null)
            {
                int i = 1;
                for (JsonNode d:
                        records) {
                    OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
                    entity.setPk(((ArrayNode)d).get(0).asText());
                    entity.setDescription(((ArrayNode)d).get(2).asText());
                    entity.setCreated(Instant.now());
                    entity.setLastUpdate(Instant.now());
                    entity.setTaxCode(((ArrayNode)d).get(3).asText());
                    entity.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
                    testDao.put(entity);
                    if (i % 100 == 0)
                        log.info("inserted " + i + "pas");
                }
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
    void updateWithDelete() throws IOException, ExecutionException, InterruptedException {

        //GIVEN
        ClassPathResource res = new ClassPathResource("src/test/resources/testdata/ipas.json");
        File file = new File(res.getPath());
        JsonNode mySchema = JsonLoader.fromFile(file);
        ArrayNode records = (ArrayNode) mySchema.get("records");

        // metto a suspended questa PA
        OnboardInstitutionEntity prev = testDao.get(((ArrayNode)records.get(6)).get(0).asText(), null); // ARNAS G. Brotzu
        prev.setLastUpdate(Instant.now());
        prev.setStatus(OnboardInstitutionEntity.STATUS_SUSPENDED);
        testDao.put(prev);

        //WHEN
        Assertions.assertDoesNotThrow(() ->
                onboardInstitutionFulltextSearchHelper.update()
        );


        // THEN
        // cerco la PA che non deve più esserci
        List<PaSummaryDto> result1 = onboardInstitutionFulltextSearchHelper.fullTextSearch(prev.getDescription().substring(0,9)).collectList().block(d);

        Optional<PaSummaryDto> mypa1 = result1.stream().filter(x -> x.getId().equals(prev.getInstitutionId())).findFirst();
        assertFalse(mypa1.isPresent());

    }


    @Test
    void updateWithAdd() throws IOException, ExecutionException, InterruptedException {

        //GIVEN
        ClassPathResource res = new ClassPathResource("src/test/resources/testdata/ipas.json");
        File file = new File(res.getPath());
        JsonNode mySchema = JsonLoader.fromFile(file);
        ArrayNode records = (ArrayNode) mySchema.get("records");

        // aggiungo una nuova PA
        OnboardInstitutionEntity prev = testDao.get(((ArrayNode)records.get(6)).get(0).asText(), null); // ARNAS G. Brotzu
        prev.setLastUpdate(Instant.now());
        prev.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        prev.setPk("TEST_ID_123");
        testDao.put(prev);

        //WHEN
        Assertions.assertDoesNotThrow(() ->
                onboardInstitutionFulltextSearchHelper.update()
        );


        // THEN
        // cerco la PA che avevo aggiunto
        List<PaSummaryDto> result1 = onboardInstitutionFulltextSearchHelper.fullTextSearch(prev.getDescription().substring(0,9)).collectList().block(d);

        Optional<PaSummaryDto> mypa1 = result1.stream().filter(x -> x.getId().equals(prev.getInstitutionId())).findFirst();
        assertTrue(mypa1.isPresent());

    }

    @Test
    void fullTextSearch() throws IOException, ExecutionException, InterruptedException {
        //GIVEN
        ClassPathResource res = new ClassPathResource("src/test/resources/testdata/ipas.json");
        File file = new File(res.getPath());
        JsonNode mySchema = JsonLoader.fromFile(file);
        ArrayNode records = (ArrayNode) mySchema.get("records");

        OnboardInstitutionEntity prev = testDao.get(((ArrayNode)records.get(8)).get(0).asText(), null); // ordine dei biologi

        //WHEN
        List<PaSummaryDto> result = onboardInstitutionFulltextSearchHelper.fullTextSearch(prev.getDescription().substring(0,8)).collectList().block(d);

        // THEN
        Optional<PaSummaryDto> mypa = result.stream().filter(x -> x.getId().equals(prev.getInstitutionId())).findFirst();
        assertTrue(mypa.isPresent());

        // WHEN
        List<PaSummaryDto> result1 = onboardInstitutionFulltextSearchHelper.fullTextSearch(prev.getDescription().substring(12,16)).collectList().block(d);

        // THEN
        Optional<PaSummaryDto> mypa1 = result1.stream().filter(x -> x.getId().equals(prev.getInstitutionId())).findFirst();
        assertTrue(mypa1.isPresent());

        // WHEN
        List<PaSummaryDto> result2 = onboardInstitutionFulltextSearchHelper.fullTextSearch("").collectList().block(d);

        // THEN
        assertTrue(result2.size() > 0);
    }
}