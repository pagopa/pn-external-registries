package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.MockProducerTest;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.TestDao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Import({LocalStackTestConfig.class, MockAWSObjectsTestConfig.class})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfoPtControllerTestIT extends MockProducerTest {

    private static final String URL = "/ext-registry-private/pt/v1/institutions";
    private static final String PT_ID = "pt-it-1";
    private static final String OTHER_PT_ID = "pt-it-2";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Autowired
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    @Autowired
    ObjectMapper objectMapper;

    TestDao<PtDelegationEntity> testDao;

    private final List<PtDelegationEntity> records = new ArrayList<>();

    @BeforeAll
    void loadRecords() throws Exception {
        testDao = new TestDao<>(dynamoDbEnhancedAsyncClient, pnExternalRegistriesConfig.getDynamodbTableNamePtDelegations(), PtDelegationEntity.class);

        try (InputStream json = new ClassPathResource("testdata/pt-delegations.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(json);
            for (JsonNode node : root.get("records")) {
                PtDelegationEntity entity = objectMapper.treeToValue(node, PtDelegationEntity.class);
                entity.setEventKey(PtDelegationEntity.buildEventKey(entity.getInstitutionId(), entity.getLastChangeAt()));
                testDao.put(entity);
                records.add(entity);
            }
        }
    }

    @AfterAll
    void deleteRecords() throws Exception {
        for (PtDelegationEntity entity : records) {
            testDao.delete(entity.getPtId(), entity.getEventKey());
        }
    }

    @Test
    void getPtInstitutionsSortedByInstitutionDescending() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri);

        assertEquals(List.of("ente-07", "ente-06", "ente-05", "ente-04", "ente-03", "ente-02", "ente-01"), institutionIds(response));
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsReturnsLatestEventOfEachInstitution() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri);

        PtInstitutionDto reactivated = institution(response, "ente-02");
        assertEquals("Comune di Bari", reactivated.getInstitutionName());
        assertEquals("80015010723", reactivated.getInstitutionTaxCode());
        assertEquals("delegation-02", reactivated.getDelegationId());
        assertEquals(PtDelegationStatusDto.ACTIVE, reactivated.getStatus());
        assertEquals(date("2026-09-02T09:00:00.200Z"), reactivated.getStatusChangedAt());

        PtInstitutionDto revoked = institution(response, "ente-03");
        assertEquals(PtDelegationStatusDto.REVOKED, revoked.getStatus());
        assertEquals(date("2026-09-15T12:30:00.600Z"), revoked.getStatusChangedAt());

        PtInstitutionDto renamed = institution(response, "ente-04");
        assertEquals("Comune di Genova", renamed.getInstitutionName());
        assertEquals(PtDelegationStatusDto.ACTIVE, renamed.getStatus());
    }

    @Test
    void getPtInstitutionsPaginated() {
        PtInstitutionsResponseDto firstPage = get(PT_ID, uri -> uri.queryParam("limit", 3));
        assertEquals(List.of("ente-07", "ente-06", "ente-05"), institutionIds(firstPage));
        assertEquals("ente-05", firstPage.getLastKey());

        PtInstitutionsResponseDto secondPage = get(PT_ID, uri -> uri.queryParam("limit", 3).queryParam("lastKey", firstPage.getLastKey()));
        assertEquals(List.of("ente-04", "ente-03", "ente-02"), institutionIds(secondPage));
        assertEquals("ente-02", secondPage.getLastKey());

        PtInstitutionsResponseDto lastPage = get(PT_ID, uri -> uri.queryParam("limit", 3).queryParam("lastKey", secondPage.getLastKey()));
        assertEquals(List.of("ente-01"), institutionIds(lastPage));
        assertNull(lastPage.getLastKey());
    }

    @Test
    void getPtInstitutionsWithoutLastKeyWhenPageIsExactlyFull() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri.queryParam("limit", 7));

        assertEquals(7, response.getItems().size());
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsFilteredByStatus() {
        PtInstitutionsResponseDto revoked = get(PT_ID, uri -> uri.queryParam("status", "REVOKED"));
        assertEquals(List.of("ente-05", "ente-03"), institutionIds(revoked));

        PtInstitutionsResponseDto active = get(PT_ID, uri -> uri.queryParam("status", "ACTIVE"));
        assertEquals(List.of("ente-07", "ente-06", "ente-04", "ente-02", "ente-01"), institutionIds(active));
    }

    @Test
    void getPtInstitutionsFilteredByStatusAndPaginated() {
        PtInstitutionsResponseDto firstPage = get(PT_ID, uri -> uri.queryParam("status", "ACTIVE").queryParam("limit", 2));
        assertEquals(List.of("ente-07", "ente-06"), institutionIds(firstPage));
        assertEquals("ente-06", firstPage.getLastKey());

        PtInstitutionsResponseDto secondPage = get(PT_ID, uri -> uri.queryParam("status", "ACTIVE").queryParam("limit", 2).queryParam("lastKey", firstPage.getLastKey()));
        assertEquals(List.of("ente-04", "ente-02"), institutionIds(secondPage));
        assertEquals("ente-02", secondPage.getLastKey());

        PtInstitutionsResponseDto lastPage = get(PT_ID, uri -> uri.queryParam("status", "ACTIVE").queryParam("limit", 2).queryParam("lastKey", secondPage.getLastKey()));
        assertEquals(List.of("ente-01"), institutionIds(lastPage));
        assertNull(lastPage.getLastKey());
    }

    @Test
    void getPtInstitutionsSearchedByNameIgnoringCase() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri.queryParam("search", "BARI"));

        assertEquals(List.of("ente-02"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedByPartialTaxCode() {
        PtInstitutionsResponseDto byOne = get(PT_ID, uri -> uri.queryParam("search", "0035104"));
        assertEquals(List.of("ente-01"), institutionIds(byOne));

        PtInstitutionsResponseDto byMany = get(PT_ID, uri -> uri.queryParam("search", "800"));
        assertEquals(List.of("ente-07", "ente-02"), institutionIds(byMany));
    }

    @Test
    void getPtInstitutionsSearchedByLatestName() {
        PtInstitutionsResponseDto latestName = get(PT_ID, uri -> uri.queryParam("search", "genova"));
        assertEquals(List.of("ente-04"), institutionIds(latestName));

        PtInstitutionsResponseDto previousName = get(PT_ID, uri -> uri.queryParam("search", "tributi"));
        assertEquals(List.of(), institutionIds(previousName));
    }

    @Test
    void getPtInstitutionsSearchedWithSurroundingSpaces() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri.queryParam("search", "  di bari  "));

        assertEquals(List.of("ente-02"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedAndFilteredByStatus() {
        PtInstitutionsResponseDto response = get(PT_ID, uri -> uri.queryParam("search", "comune").queryParam("status", "REVOKED"));

        assertEquals(List.of("ente-03"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsOfOtherPt() {
        PtInstitutionsResponseDto response = get(OTHER_PT_ID, uri -> uri);

        assertEquals(List.of("ente-02"), institutionIds(response));
        assertEquals("delegation-08", response.getItems().get(0).getDelegationId());
    }

    @Test
    void getPtInstitutionsOfPtWithoutDelegations() {
        PtInstitutionsResponseDto response = get("pt-it-unknown", uri -> uri);

        assertEquals(List.of(), institutionIds(response));
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsWithInvalidParameters() {
        badRequest(uri -> uri.queryParam("limit", 0));
        badRequest(uri -> uri.queryParam("limit", 51));
        badRequest(uri -> uri.queryParam("search", "ba"));
        badRequest(uri -> uri.queryParam("status", "DELETED"));
    }

    private PtInstitutionsResponseDto get(String ptId, Function<UriBuilder, UriBuilder> query) {
        return webTestClient.get()
                .uri(uriBuilder -> build(uriBuilder, query))
                .headers(headers -> contextHeaders(headers, ptId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PtInstitutionsResponseDto.class)
                .returnResult()
                .getResponseBody();
    }

    private void badRequest(Function<UriBuilder, UriBuilder> query) {
        webTestClient.get()
                .uri(uriBuilder -> build(uriBuilder, query))
                .headers(headers -> contextHeaders(headers, PT_ID))
                .exchange()
                .expectStatus().isBadRequest();
    }

    private URI build(UriBuilder uriBuilder, Function<UriBuilder, UriBuilder> query) {
        return query.apply(uriBuilder.path(URL)).build();
    }

    private void contextHeaders(HttpHeaders headers, String ptId) {
        headers.add("x-pagopa-pn-cx-id", ptId);
        headers.add("x-pagopa-pn-cx-type", "PT");
        headers.add("x-pagopa-pn-uid", "user-it");
        headers.add("x-pagopa-pn-src-ch", "WEB");
    }

    private List<String> institutionIds(PtInstitutionsResponseDto response) {
        return response.getItems().stream().map(PtInstitutionDto::getInstitutionId).toList();
    }

    private PtInstitutionDto institution(PtInstitutionsResponseDto response, String institutionId) {
        return response.getItems().stream()
                .filter(item -> institutionId.equals(item.getInstitutionId()))
                .findFirst()
                .orElseThrow();
    }

    private Date date(String instant) {
        return Date.from(Instant.parse(instant));
    }
}
