package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.MockProducerTest;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({LocalStackTestConfig.class, MockAWSObjectsTestConfig.class})
class PtDelegationsDaoTestIT extends MockProducerTest {

    @Autowired
    private PtDelegationsDao ptDelegationsDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    TestDao<PtDelegationEntity> testDao;

    private static final String CREATED_AT = "2026-11-02T10:00:00.000000Z";

    private final List<PtDelegationEntity> created = new ArrayList<>();

    @BeforeEach
    void setup() {
        testDao = new TestDao<>(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNamePtDelegations(), PtDelegationEntity.class);
    }

    @AfterEach
    void clean() {
        created.forEach(entity -> {
            try {
                testDao.delete(entity.getPtId(), entity.getEventKey());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        });
        created.clear();
    }

    @Test
    void insertIfAbsent() throws Exception {
        PtDelegationEntity entity = added(UUID.randomUUID().toString(), "ente-A", CREATED_AT);

        Boolean result = ptDelegationsDao.insertIfAbsent(entity).block();

        Assertions.assertEquals(Boolean.TRUE, result);
        Assertions.assertEquals(entity, testDao.get(entity.getPtId(), entity.getEventKey()));
    }

    @Test
    void insertIfAbsentDoesNotOverwriteExistingEvent() throws Exception {
        String ptId = UUID.randomUUID().toString();
        PtDelegationEntity existing = put(updated(ptId, "ente-A", PtDelegationEntity.STATUS_DELETED, "2026-11-20T09:30:00.000000Z"));
        PtDelegationEntity duplicated = updated(ptId, "ente-A", PtDelegationEntity.STATUS_DELETED, "2026-11-20T09:30:00.000000Z");

        Boolean result = ptDelegationsDao.insertIfAbsent(duplicated).block();

        Assertions.assertEquals(Boolean.FALSE, result);
        Assertions.assertNotEquals(existing.getSourceEventId(), duplicated.getSourceEventId());
        Assertions.assertEquals(existing, testDao.get(ptId, existing.getEventKey()));
    }

    @Test
    void getLatestEventPerInstitution() throws Exception {
        String ptId = UUID.randomUUID().toString();
        put(added(ptId, "ente-A", CREATED_AT));
        put(updated(ptId, "ente-A", PtDelegationEntity.STATUS_DELETED, "2026-11-20T09:30:00.000000Z"));
        PtDelegationEntity latestA = put(updated(ptId, "ente-A", PtDelegationEntity.STATUS_ACTIVE, "2026-12-01T15:12:00.000000Z"));
        PtDelegationEntity latestB = put(added(ptId, "ente-B", "2026-11-05T08:00:00.000000Z"));
        put(added(UUID.randomUUID().toString(), "ente-C", "2026-11-05T08:00:00.000000Z"));

        List<PtDelegationEntity> result = ptDelegationsDao.getLatestEventPerInstitution(ptId, null).collectList().block();

        assertThat(result).containsExactlyInAnyOrder(latestA, latestB);
    }

    @Test
    void getLatestEventPerInstitutionFromLastInstitution() throws Exception {
        String ptId = UUID.randomUUID().toString();
        put(added(ptId, "ente-A", CREATED_AT));
        PtDelegationEntity latestA = put(updated(ptId, "ente-A", PtDelegationEntity.STATUS_DELETED, "2026-11-20T09:30:00.000000Z"));
        put(added(ptId, "ente-B", "2026-11-05T08:00:00.000000Z"));
        put(added(ptId, "ente-B1", "2026-11-06T08:00:00.000000Z"));
        put(added(ptId, "ente-C", "2026-11-07T08:00:00.000000Z"));

        List<PtDelegationEntity> result = ptDelegationsDao.getLatestEventPerInstitution(ptId, "ente-B").collectList().block();

        assertThat(result).containsExactly(latestA);
    }

    @Test
    void getLatestEventPerInstitutionFromLastInstitutionWithoutOthers() throws Exception {
        String ptId = UUID.randomUUID().toString();
        put(added(ptId, "ente-A", CREATED_AT));

        List<PtDelegationEntity> result = ptDelegationsDao.getLatestEventPerInstitution(ptId, "ente-A").collectList().block();

        Assertions.assertEquals(List.of(), result);
    }

    @Test
    void getLatestEventPerInstitutionWithoutEvents() {
        List<PtDelegationEntity> result = ptDelegationsDao.getLatestEventPerInstitution(UUID.randomUUID().toString(), null).collectList().block();

        Assertions.assertEquals(List.of(), result);
    }

    @Test
    void getLatestEventPerInstitutionIgnoresLateEvent() throws Exception {
        String ptId = UUID.randomUUID().toString();
        PtDelegationEntity latest = put(updated(ptId, "ente-A", PtDelegationEntity.STATUS_DELETED, "2026-11-20T09:30:00.000000Z"));
        put(added(ptId, "ente-A", CREATED_AT));

        List<PtDelegationEntity> result = ptDelegationsDao.getLatestEventPerInstitution(ptId, null).collectList().block();

        Assertions.assertEquals(List.of(latest), result);
    }

    private PtDelegationEntity put(PtDelegationEntity entity) throws Exception {
        testDao.put(entity);
        return entity;
    }

    private PtDelegationEntity added(String ptId, String institutionId, String createdAt) {
        PtDelegationEntity entity = newEntity(ptId, institutionId, PtDelegationEntity.STATUS_ACTIVE, createdAt);
        entity.setSourceEventType("ADD");
        entity.setCreatedAt(Instant.parse(createdAt));
        return entity;
    }

    private PtDelegationEntity updated(String ptId, String institutionId, String status, String updatedAt) {
        PtDelegationEntity entity = newEntity(ptId, institutionId, status, updatedAt);
        entity.setSourceEventType("UPDATE");
        entity.setCreatedAt(Instant.parse(CREATED_AT));
        entity.setUpdatedAt(Instant.parse(updatedAt));
        if (PtDelegationEntity.STATUS_DELETED.equals(status)) {
            entity.setClosedAt(Instant.parse(updatedAt));
        }
        return entity;
    }

    private PtDelegationEntity newEntity(String ptId, String institutionId, String status, String lastChangeAt) {
        PtDelegationEntity entity = new PtDelegationEntity();
        entity.setPtId(ptId);
        entity.setEventKey(institutionId + "#" + lastChangeAt);
        entity.setDelegationId("delegation-" + institutionId);
        entity.setDelegationType("PT");
        entity.setProductId("prod-pn");
        entity.setStatus(status);
        entity.setIsTest(false);
        entity.setSourceEventId(UUID.randomUUID().toString());
        entity.setLastChangeAt(Instant.parse(lastChangeAt));
        entity.setInstitutionId(institutionId);
        entity.setInstitutionName("Comune di " + institutionId);
        entity.setInstitutionTaxCode("00000000000");
        entity.setInstitutionType("PA");
        entity.setPtName("Partner Tecnologico");
        entity.setPtTaxCode("11111111111");
        entity.setPtType("PT");
        created.add(entity);
        return entity;
    }
}
