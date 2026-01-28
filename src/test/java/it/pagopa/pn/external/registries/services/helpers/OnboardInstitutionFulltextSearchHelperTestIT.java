package it.pagopa.pn.external.registries.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonLoader;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import it.pagopa.pn.external.registries.services.helpers.impl.OnboardInstitutionFulltextSearchHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import(LocalStackTestConfig.class)
@Slf4j
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnboardInstitutionFulltextSearchHelperTestIT {
    final String classPathTestData = "src/test/resources/testdata/ipas.json";
    final String classPathTestAllData = "src/test/resources/testdata/ipas_all.json";

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


    @AfterAll
    public void clean() {
        cleanData(classPathTestData);
//        cleanData(classPathTestAllData);
    }

    void cleanData(String classPath) {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), OnboardInstitutionEntity.class);

        try {
            ClassPathResource res = new ClassPathResource(classPath);
            File file = new File(res.getPath());
            JsonNode mySchema = JsonLoader.fromFile(file);
            ArrayNode records = (ArrayNode) mySchema.get("records");

            OnboardInstitutionEntity prev = testDao.get((records.get(0)).get(0).asText(), null);
            if (prev != null) {
                int i = 1;
                for (JsonNode jsonNode: records) {
                    testDao.delete(jsonNode.get(0).asText(),null);
                    if (i % 100 == 0)
                        log.info("deleted " + i + "pas");
                    i++;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @BeforeEach
    public void beforeEach() throws IOException, ExecutionException, InterruptedException {
        loadIpas(classPathTestData, false, null);

//        Map<String, String> taxCodeToRootPk = getTaxCodeFathers();
//        loadIpas(classPathTestAllData, true, taxCodeToRootPk);
        loadFatherChild();

        onboardInstitutionFulltextSearchHelper.update();
    }

    private void loadIpas(String classPath, boolean fatherChildren, Map<String, String> taxCodeToRootPk) throws IOException {
        testDao = new TestDao(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), OnboardInstitutionEntity.class);
        try {
            ClassPathResource res = new ClassPathResource(classPath);
            File file = new File(res.getPath());
            JsonNode mySchema = JsonLoader.fromFile(file);
            ArrayNode records = (ArrayNode) mySchema.get("records");

            for (JsonNode jsonNode : records) {
                String pk = jsonNode.get(0).asText();
                OnboardInstitutionEntity existingEntity = testDao.get(pk, null);
                if (existingEntity == null) {
                    testDao.put(getEntity(jsonNode, taxCodeToRootPk, fatherChildren));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private OnboardInstitutionEntity getEntity(JsonNode jsonNode, Map<String, String> taxCodeToRootPk, boolean fatherChildren) {
        OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
        String pk = jsonNode.get(0).asText();
        String taxCode = jsonNode.get(3).asText();

        entity.setPk(pk);
        entity.setDescription(jsonNode.get(2).asText());
        entity.setCreated(Instant.now());
        entity.setLastUpdate(Instant.now());
        entity.setTaxCode(taxCode);
        entity.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        entity.setOnlyRootStatus(OnboardInstitutionEntity.STATUS_ACTIVE);

        if (fatherChildren && taxCodeToRootPk != null && taxCodeToRootPk.containsKey(taxCode)) {
            entity.setRootId(taxCodeToRootPk.get(taxCode));  // Assegna la pk del padre
        } else {
            entity.setRootId(pk); // Se non è figlio, assegna rootId a se stesso
        }

        return entity;
    }

    private Map<String, String> getTaxCodeFathers() throws IOException {
        Map<String, String> taxCodeToRootPk = new HashMap<>();

        ClassPathResource res = new ClassPathResource(classPathTestAllData);
        File file = new File(res.getPath());
        JsonNode mySchema = JsonLoader.fromFile(file);
        ArrayNode records = (ArrayNode) mySchema.get("records");

        for (JsonNode jsonNode : records) {
            String pk = jsonNode.get(0).asText();
            String taxCode = jsonNode.get(3).asText();
            taxCodeToRootPk.putIfAbsent(taxCode, pk);
        }

        return taxCodeToRootPk;
    }

    private void loadFatherChild() throws ExecutionException, InterruptedException {
        // Entità padre
        OnboardInstitutionEntity parentEntity = createFatherOrChild("PARENT_001", "PARENT_001", "Ministero della Salute", "TAXCODE_PARENT_001");
        testDao.put(parentEntity);

        // Entità figlio collegata al padre
        OnboardInstitutionEntity childEntity = createFatherOrChild("CHILD_001", "PARENT_001", "Azienda Sanitaria Locale Roma 1", "TAXCODE_CHILD_001");
        testDao.put(childEntity);

        // Un altro set di padre-figlio
        OnboardInstitutionEntity parentEntity2 = createFatherOrChild("PARENT_002", "PARENT_002", "Ministero delle Finanze", "TAXCODE_PARENT_002");
        testDao.put(parentEntity2);

        OnboardInstitutionEntity childEntity2 = createFatherOrChild("CHILD_002", "PARENT_002", "Agenzia delle Entrate", "TAXCODE_CHILD_002");
        testDao.put(childEntity2);
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
        ClassPathResource res = new ClassPathResource(classPathTestData);
        File file = new File(res.getPath());
        JsonNode mySchema = JsonLoader.fromFile(file);
        ArrayNode records = (ArrayNode) mySchema.get("records");

        // metto a suspended questa PA
        OnboardInstitutionEntity prev = testDao.get(((ArrayNode)records.get(6)).get(0).asText(), null); // ARNAS G. Brotzu
        prev.setLastUpdate(Instant.now());
        prev.setStatus(OnboardInstitutionEntity.STATUS_CLOSED);
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
        ClassPathResource res = new ClassPathResource(classPathTestData);
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

        // clean
        testDao.delete(prev.getPk(),null);
    }

    @Test
    void fullTextSearch() throws IOException, ExecutionException, InterruptedException {
        //GIVEN
        ClassPathResource res = new ClassPathResource(classPathTestData);
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


    @Test
    void extendedFullTextSearch() {
        // WHEN
        List<PaSummaryExtendedDto> result = onboardInstitutionFulltextSearchHelper
                .extendedFullTextSearch("", false)
                .collectList()
                .block();

        // THEN
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Si contano padri e figli
        long numParents = result.stream().filter(dto -> dto.getChildrenList() != null && !dto.getChildrenList().isEmpty()).count();
        long numEntitiesWithoutChildren = result.stream().filter(dto -> dto.getChildrenList() == null || dto.getChildrenList().isEmpty()).count();

        assertTrue(numParents > 0);
        assertTrue(numEntitiesWithoutChildren > 0);
    }

    @Test
    void extendedFullTextSearch_onlyChildren() {
        // WHEN
        PaSummaryExtendedDto result = onboardInstitutionFulltextSearchHelper
                .extendedFullTextSearch("", true)
                .blockFirst();

        // THEN
        assertNotNull(result);
        assertNotNull(result.getChildrenList());
        assertFalse(result.getChildrenList().isEmpty());

        // Si verifica che la lista contenga figli
        boolean containsEntitiesWithChildren = !result.getChildrenList().isEmpty();
        assertTrue(containsEntitiesWithChildren);
    }

    @Test
    void extendedFullTextSearch_partialMatch() {
        // WHEN
        List<PaSummaryExtendedDto> allResults = onboardInstitutionFulltextSearchHelper
                .extendedFullTextSearch("", false)
                .collectList()
                .block();

        assertNotNull(allResults);
        assertFalse(allResults.isEmpty());

        PaSummaryExtendedDto selectedEntity = allResults.get(0);
        String entityName = selectedEntity.getName();

        assertNotNull(entityName);
        assertTrue(entityName.length() >= 3);

        // WHEN: Si ricerca un substring del nome
        String partialQuery = entityName.substring(0, 3);
        List<PaSummaryExtendedDto> resultWithSubstring = onboardInstitutionFulltextSearchHelper
                .extendedFullTextSearch(partialQuery, false)
                .collectList()
                .block();

        // THEN
        assertNotNull(resultWithSubstring);
        assertFalse(resultWithSubstring.isEmpty());

        boolean containsMatchingEntity = resultWithSubstring.stream()
                .anyMatch(dto -> dto.getName().toLowerCase().contains(partialQuery.toLowerCase()));

        assertTrue(containsMatchingEntity, "Almeno un risultato dovrebbe contenere la substring '" + partialQuery + "'");
    }

    private OnboardInstitutionEntity createFatherOrChild(String pk, String rootId, String name, String taxCode) {
        OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
        entity.setPk(pk);
        entity.setDescription(name);
        entity.setCreated(Instant.now());
        entity.setLastUpdate(Instant.now());
        entity.setTaxCode(taxCode);
        entity.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        entity.setOnlyRootStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        entity.setRootId(rootId);
        return entity;
    }
}