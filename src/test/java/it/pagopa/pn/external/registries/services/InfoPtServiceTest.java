package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.middleware.db.dao.PtDelegationsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class InfoPtServiceTest {

    private static final String PT_ID = "pt-1";

    private InfoPtService infoPtService;

    @Mock
    private PtDelegationsDao ptDelegationsDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        infoPtService = new InfoPtService(ptDelegationsDao);
    }

    @Test
    void getPtInstitutions() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_DELETED)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, null, 10, null).block();

        assertEquals(List.of("ente-C", "ente-B"), institutionIds(response));
        assertEquals(PtDelegationStatusDto.REVOKED, response.getItems().get(1).getStatus());
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsWithoutDelegations() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null)).thenReturn(Flux.empty());

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, null, 10, null).block();

        assertEquals(List.of(), response.getItems());
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsReturnsLastKeyWhenOtherInstitutionsFollow() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-A", "Comune di Ancona", "11111111111", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, null, 2, null).block();

        assertEquals(List.of("ente-C", "ente-B"), institutionIds(response));
        assertEquals("ente-B", response.getLastKey());
    }

    @Test
    void getPtInstitutionsWithoutLastKeyWhenPageIsExactlyFull() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, null, 2, null).block();

        assertEquals(List.of("ente-C", "ente-B"), institutionIds(response));
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsFromLastKey() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, "ente-B"))
                .thenReturn(Flux.just(newEntity("ente-A", "Comune di Ancona", "11111111111", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, null, 2, "ente-B").block();

        assertEquals(List.of("ente-A"), institutionIds(response));
        assertNull(response.getLastKey());
    }

    @Test
    void getPtInstitutionsStopsReadingAfterNextPageStart() {
        AtomicInteger read = new AtomicInteger();
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-D", "Comune di Dolo", "44444444444", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-A", "Comune di Ancona", "11111111111", PtDelegationEntity.STATUS_ACTIVE))
                        .doOnNext(entity -> read.incrementAndGet()));

        infoPtService.getPtInstitutions(PT_ID, null, null, 2, null).block();

        assertEquals(3, read.get());
    }

    @Test
    void getPtInstitutionsFilteredByStatus() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_DELETED),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-A", "Comune di Ancona", "11111111111", PtDelegationEntity.STATUS_DELETED)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, PtDelegationStatusDto.REVOKED, 10, null).block();

        assertEquals(List.of("ente-C", "ente-A"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedByName() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, "BARI", null, 10, null).block();

        assertEquals(List.of("ente-B"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedByTaxCode() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, "3333", null, 10, null).block();

        assertEquals(List.of("ente-C"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedWithSurroundingSpaces() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, "  di bari  ", null, 10, null).block();

        assertEquals(List.of("ente-B"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsSearchedWithBlankValue() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, "   ", null, 10, null).block();

        assertEquals(List.of("ente-C", "ente-B"), institutionIds(response));
    }

    @Test
    void getPtInstitutionsFilteredReturnsLastKeyOfLastMatchingInstitution() {
        when(ptDelegationsDao.getLatestEventPerInstitution(PT_ID, null))
                .thenReturn(Flux.just(
                        newEntity("ente-D", "Comune di Dolo", "44444444444", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-C", "Comune di Cagliari", "33333333333", PtDelegationEntity.STATUS_DELETED),
                        newEntity("ente-B", "Comune di Bari", "22222222222", PtDelegationEntity.STATUS_ACTIVE),
                        newEntity("ente-A", "Comune di Ancona", "11111111111", PtDelegationEntity.STATUS_ACTIVE)));

        PtInstitutionsResponseDto response = infoPtService.getPtInstitutions(PT_ID, null, PtDelegationStatusDto.ACTIVE, 2, null).block();

        assertEquals(List.of("ente-D", "ente-B"), institutionIds(response));
        assertEquals("ente-B", response.getLastKey());
    }

    private List<String> institutionIds(PtInstitutionsResponseDto response) {
        return response.getItems().stream().map(PtInstitutionDto::getInstitutionId).toList();
    }

    private PtDelegationEntity newEntity(String institutionId, String institutionName, String institutionTaxCode, String status) {
        Instant createdAt = Instant.parse("2026-11-02T10:00:00.000000Z");
        PtDelegationEntity entity = new PtDelegationEntity();
        entity.setPtId(PT_ID);
        entity.setEventKey(PtDelegationEntity.buildEventKey(institutionId, createdAt));
        entity.setInstitutionId(institutionId);
        entity.setInstitutionName(institutionName);
        entity.setInstitutionTaxCode(institutionTaxCode);
        entity.setDelegationId("delegation-" + institutionId);
        entity.setStatus(status);
        entity.setCreatedAt(createdAt);
        entity.setLastChangeAt(createdAt);
        if (PtDelegationEntity.STATUS_DELETED.equals(status)) {
            entity.setClosedAt(createdAt);
        }
        return entity;
    }
}
