package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;
class CostComponentServiceTest {

    @Mock
    CostComponentsDao costComponentsDao;

    private final CostComponentsMapper costComponentsMapper = new CostComponentsMapper();

    CostComponentService costComponentService;

    private final String pk = "iun##recIndex";
    private final String sk = "creditorTaxId + \"##\" + noticeCode";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
    }

    @Test
    void getTotalCostTest() {
        // Given
        String iun = "testIun";
        String recIndex = "testRecIndex";
        String creditorTaxId = "testTaxId";
        String noticeCode = "testNoticeCode";
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        CostComponentsEntity entity = new CostComponentsEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setBaseCost(100);
        entity.setSimpleRegisteredLetterCost(50);
        entity.setFirstAnalogCost(25);
        entity.setSecondAnalogCost(25);
        entity.setIsRefusedCancelled(false);

        when(costComponentsDao.getItem(pk, sk)).thenReturn(Mono.just(entity));

        // When
        Integer totalCost = costComponentService.getTotalCost(iun, recIndex, creditorTaxId, noticeCode).block();

        // Then
        Assertions.assertNotNull(totalCost, "Total cost should not be null");
        Assertions. assertEquals(200, totalCost, "The total cost should be 200");

        verify(costComponentsDao, times(1)).getItem(pk, sk);
    }

    @Test
    void getTotalCost_AllNulls_Test() {
        // Given
        String iun = "testIun";
        String recIndex = "testRecIndex";
        String creditorTaxId = "testTaxId";
        String noticeCode = "testNoticeCode";
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        CostComponentsEntity entity = new CostComponentsEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setBaseCost(null);
        entity.setSimpleRegisteredLetterCost(null);
        entity.setFirstAnalogCost(null);
        entity.setSecondAnalogCost(null);
        entity.setIsRefusedCancelled(false);

        when(costComponentsDao.getItem(pk, sk)).thenReturn(Mono.just(entity));

        // When
        Integer totalCost = costComponentService.getTotalCost(iun, recIndex, creditorTaxId, noticeCode).block();

        // Then
        Assertions.assertNotNull(totalCost, "Total cost should not be null");
        Assertions. assertEquals(0, totalCost, "The total cost should be 0");

        verify(costComponentsDao, times(1)).getItem(pk, sk);
    }

    @Test
    void getTotalCost_RefusedOrCancelled() {
        // Given
        String iun = "testIun";
        String recIndex = "testRecIndex";
        String creditorTaxId = "testTaxId";
        String noticeCode = "testNoticeCode";
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        CostComponentsEntity entity = new CostComponentsEntity();
        entity.setPk(pk);
        entity.setSk(sk);
        entity.setBaseCost(100);
        entity.setSimpleRegisteredLetterCost(50);
        entity.setFirstAnalogCost(25);
        entity.setSecondAnalogCost(25);
        entity.setIsRefusedCancelled(true);

        when(costComponentsDao.getItem(pk, sk)).thenReturn(Mono.just(entity));

        // When
        Integer totalCost = costComponentService.getTotalCost(iun, recIndex, creditorTaxId, noticeCode).block();

        // Then
        Assertions.assertNotNull(totalCost, "Total cost should not be null");
        Assertions.assertEquals(0, totalCost, "The total cost should be 0");

        verify(costComponentsDao, times(1)).getItem(pk, sk);
    }

    @Test
    void getIuvsForIunAndRecIndexTest() {
        // Given
        String iun = "iun";
        String recIndex = "recIndex";
        String creditorTaxId = "creditorTaxId";
        String creditorTaxId2 = "creditorTaxId2";
        String noticeCode = "noticeCode";
        String noticeCode2 = "noticeCode2";
        String pk = iun + "##" + recIndex;

        CostComponentsEntity entity1 = newCostComponentsEntity();
        CostComponentsEntity entity2 = newCostComponentsEntity();
        entity2.setSk("creditorTaxId2##noticeCode2");

        when(costComponentsDao.getItems(pk)).thenReturn(Flux.just(entity1, entity2));

        // When
        List<CostComponentsInt> resultList = costComponentService.getIuvsForIunAndRecIndex(iun, recIndex).collectList().block();

        // Then
        Assertions.assertNotNull(resultList, "Result list should not be null");
        Assertions.assertEquals(2, resultList.size(), "Result list size should be 2");

        CostComponentsInt costComponentsInt = resultList.get(0);
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(creditorTaxId, costComponentsInt.getCreditorTaxId(), "creditorTaxId should match");
        Assertions.assertEquals(noticeCode, costComponentsInt.getNoticeCode(), "noticeCode should match");

        costComponentsInt = resultList.get(1);
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(creditorTaxId2, costComponentsInt.getCreditorTaxId(), "creditorTaxId should match");
        Assertions.assertEquals(noticeCode2, costComponentsInt.getNoticeCode(), "noticeCode should match");

        verify(costComponentsDao, times(1)).getItems(pk);
    }

    @Test
    void getIuvsForIunAndRecIndex_IllegalPkSkTest() {
        // Given
        String iun = "iun";
        String recIndex = "recIndex";
        String creditorTaxId = "creditorTaxId";
        String noticeCode = "noticeCode";
        String pk = iun + "##" + recIndex;

        CostComponentsEntity entity1 = newCostComponentsEntity();
        entity1.setPk("iun##recIndex##extra");
        entity1.setSk("creditorTaxId##noticeCode##extra");

        when(costComponentsDao.getItems(pk)).thenReturn(Flux.just(entity1));

        // When
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            costComponentService.getIuvsForIunAndRecIndex(iun, recIndex).collectList().block();
        });

        entity1.setPk("iun");
        entity1.setSk("creditorTaxId");

        when(costComponentsDao.getItems(pk)).thenReturn(Flux.just(entity1));

        // When
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            costComponentService.getIuvsForIunAndRecIndex(iun, recIndex).collectList().block();
        });
    }

    private CostComponentsEntity newCostComponentsEntity() {
        CostComponentsEntity entity = new CostComponentsEntity();

        // the pk and sk must be in the correct for, for the mapper not to fail
        entity.setPk("iun##recIndex");
        entity.setSk("creditorTaxId##noticeCode");

        entity.setBaseCost(0);
        entity.setSimpleRegisteredLetterCost(0);
        entity.setFirstAnalogCost(0);
        entity.setSecondAnalogCost(0);
        entity.setIsRefusedCancelled(false);
        return entity;
    }
}
