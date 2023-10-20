package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
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

    private final String iun = "iun";
    private final int recIndex = 0;
    private final String creditorTaxId = "testTaxId";
    private final String noticeCode = "testNoticeCode";

    private final String pk = iun + "##" + recIndex;
    private final String sk = creditorTaxId + "##" + noticeCode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
    }

    @Test
    void insertStepCost_ValidationTest() {
        // Given
        Integer notificationStepCost = 100;

        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setBaseCost(notificationStepCost);

        when(costComponentsDao.insertOrUpdate(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity));
        when(costComponentsDao.updateNotNull(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity)); // it shouldn't be called

        ArgumentCaptor<CostComponentsEntity> captor = ArgumentCaptor.forClass(CostComponentsEntity.class);

        // When
        CostComponentsInt costComponentsInt = costComponentService
                .insertStepCost(CostUpdateCostPhaseInt.VALIDATION, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost)
                .block();

        // Then
        Assertions.assertNotNull(costComponentsInt, "Result should not be null");
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(notificationStepCost, costComponentsInt.getBaseCost(), "Base cost should match");
        Assertions.assertEquals(0, costComponentsInt.getSimpleRegisteredLetterCost(), "Simple Registered Letter Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getFirstAnalogCost(), "First Analog Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getSecondAnalogCost(), "Second Analog Cost should be 0");
        Assertions.assertFalse(costComponentsInt.getIsRefusedCancelled(), "Is Refused Cancelled should be false");

        verify(costComponentsDao, times(1)).insertOrUpdate(captor.capture());
        verify(costComponentsDao, times(0)).updateNotNull(any(CostComponentsEntity.class));

        CostComponentsEntity capturedEntity = captor.getValue();
        Assertions.assertEquals(notificationStepCost, capturedEntity.getBaseCost());
        Assertions.assertEquals(0, capturedEntity.getSimpleRegisteredLetterCost());
        Assertions.assertEquals(0, capturedEntity.getFirstAnalogCost());
        Assertions.assertEquals(0, capturedEntity.getSecondAnalogCost());
        Assertions.assertFalse(capturedEntity.getIsRefusedCancelled());
    }

    @Test
    void insertStepCost_RequestRefusedOrNotificationCancelledTest() {
        // Given
        Integer notificationStepCost = 100;  // This value should be ignored as the request is refused

        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setIsRefusedCancelled(true);

        when(costComponentsDao.insertOrUpdate(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity));
        when(costComponentsDao.updateNotNull(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity)); // it shouldn't be called

        ArgumentCaptor<CostComponentsEntity> captor = ArgumentCaptor.forClass(CostComponentsEntity.class);

        // When
        CostComponentsInt costComponentsInt = costComponentService
                .insertStepCost(CostUpdateCostPhaseInt.REQUEST_REFUSED, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost)
                .block(); // replace REQUEST_REFUSED with NOTIFICATION_CANCELLED to test the other case: the behavior is the same

        // Then
        Assertions.assertNotNull(costComponentsInt, "Result should not be null");
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(0, costComponentsInt.getBaseCost(), "Base cost should be 0 as the request is refused");
        Assertions.assertEquals(0, costComponentsInt.getSimpleRegisteredLetterCost(), "Simple Registered Letter Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getFirstAnalogCost(), "First Analog Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getSecondAnalogCost(), "Second Analog Cost should be 0");
        Assertions.assertTrue(costComponentsInt.getIsRefusedCancelled(), "Is Refused Cancelled should be true");

        verify(costComponentsDao, times(1)).insertOrUpdate(captor.capture());
        verify(costComponentsDao, times(0)).updateNotNull(any(CostComponentsEntity.class));

        CostComponentsEntity capturedEntity = captor.getValue();
        Assertions.assertEquals(0, capturedEntity.getBaseCost());
        Assertions.assertEquals(0, capturedEntity.getSimpleRegisteredLetterCost());
        Assertions.assertEquals(0, capturedEntity.getFirstAnalogCost());
        Assertions.assertEquals(0, capturedEntity.getSecondAnalogCost());
        Assertions.assertTrue(capturedEntity.getIsRefusedCancelled());
    }

    @Test
    void insertStepCost_SendSimpleRegisteredLetterTest() {
        // Given
        Integer notificationStepCost = 100;

        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setSimpleRegisteredLetterCost(notificationStepCost);

        when(costComponentsDao.insertOrUpdate(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity)); // it shouldn't be called
        when(costComponentsDao.updateNotNull(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity));

        ArgumentCaptor<CostComponentsEntity> captor = ArgumentCaptor.forClass(CostComponentsEntity.class);

        // When
        CostComponentsInt costComponentsInt = costComponentService
                .insertStepCost(CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost)
                .block();

        // Then
        Assertions.assertNotNull(costComponentsInt, "Result should not be null");
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(0, costComponentsInt.getBaseCost(), "Base cost should be 0");
        Assertions.assertEquals(notificationStepCost, costComponentsInt.getSimpleRegisteredLetterCost(), "Simple Registered Letter Cost should match");
        Assertions.assertEquals(0, costComponentsInt.getFirstAnalogCost(), "First Analog Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getSecondAnalogCost(), "Second Analog Cost should be 0");
        Assertions.assertFalse(costComponentsInt.getIsRefusedCancelled(), "Is Refused Cancelled should be false");

        verify(costComponentsDao, times(0)).insertOrUpdate(any(CostComponentsEntity.class));
        verify(costComponentsDao, times(1)).updateNotNull(captor.capture());

        CostComponentsEntity capturedEntity = captor.getValue();
        Assertions.assertNull(capturedEntity.getBaseCost());
        Assertions.assertEquals(notificationStepCost, capturedEntity.getSimpleRegisteredLetterCost());
        Assertions.assertNull(capturedEntity.getFirstAnalogCost());
        Assertions.assertNull(capturedEntity.getSecondAnalogCost());
        Assertions.assertNull(capturedEntity.getIsRefusedCancelled());
    }

    @Test
    void insertStepCost_SendAnalogDomicileAttempt0Test() {
        // Given
        Integer notificationStepCost = 100;

        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setFirstAnalogCost(notificationStepCost);

        when(costComponentsDao.insertOrUpdate(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity)); // it shouldn't be called
        when(costComponentsDao.updateNotNull(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity));

        ArgumentCaptor<CostComponentsEntity> captor = ArgumentCaptor.forClass(CostComponentsEntity.class);

        // When
        CostComponentsInt costComponentsInt = costComponentService
                .insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost)
                .block();

        // Then
        Assertions.assertNotNull(costComponentsInt, "Result should not be null");
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(0, costComponentsInt.getBaseCost(), "Base cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getSimpleRegisteredLetterCost(), "Simple Registered Letter Cost should be 0");
        Assertions.assertEquals(notificationStepCost, costComponentsInt.getFirstAnalogCost(), "First Analog Cost should match");
        Assertions.assertEquals(0, costComponentsInt.getSecondAnalogCost(), "Second Analog Cost should be 0");
        Assertions.assertFalse(costComponentsInt.getIsRefusedCancelled(), "Is Refused Cancelled should be false");

        verify(costComponentsDao, times(0)).insertOrUpdate(any(CostComponentsEntity.class));
        verify(costComponentsDao, times(1)).updateNotNull(captor.capture());

        CostComponentsEntity capturedEntity = captor.getValue();
        Assertions.assertNull(capturedEntity.getBaseCost());
        Assertions.assertNull(capturedEntity.getSimpleRegisteredLetterCost());
        Assertions.assertEquals(notificationStepCost, capturedEntity.getFirstAnalogCost());
        Assertions.assertNull(capturedEntity.getSecondAnalogCost());
        Assertions.assertNull(capturedEntity.getIsRefusedCancelled());
    }

    @Test
    void insertStepCost_SendAnalogDomicileAttempt1Test() {
        // Given
        Integer notificationStepCost = 150;

        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setSecondAnalogCost(notificationStepCost);

        when(costComponentsDao.insertOrUpdate(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity)); // it shouldn't be called
        when(costComponentsDao.updateNotNull(any(CostComponentsEntity.class))).thenReturn(Mono.just(entity));

        ArgumentCaptor<CostComponentsEntity> captor = ArgumentCaptor.forClass(CostComponentsEntity.class);

        // When
        CostComponentsInt costComponentsInt = costComponentService
                .insertStepCost(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_1, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost)
                .block();

        // Then
        Assertions.assertNotNull(costComponentsInt, "Result should not be null");
        Assertions.assertEquals(iun, costComponentsInt.getIun(), "IUN should match");
        Assertions.assertEquals(recIndex, costComponentsInt.getRecIndex(), "recIndex should match");
        Assertions.assertEquals(0, costComponentsInt.getBaseCost(), "Base cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getSimpleRegisteredLetterCost(), "Simple Registered Letter Cost should be 0");
        Assertions.assertEquals(0, costComponentsInt.getFirstAnalogCost(), "First Analog Cost should be 0");
        Assertions.assertEquals(notificationStepCost, costComponentsInt.getSecondAnalogCost(), "Second Analog Cost should match");
        Assertions.assertFalse(costComponentsInt.getIsRefusedCancelled(), "Is Refused Cancelled should be false");

        verify(costComponentsDao, times(0)).insertOrUpdate(any(CostComponentsEntity.class));
        verify(costComponentsDao, times(1)).updateNotNull(captor.capture());

        CostComponentsEntity capturedEntity = captor.getValue();
        Assertions.assertNull(capturedEntity.getBaseCost());
        Assertions.assertNull(capturedEntity.getSimpleRegisteredLetterCost());
        Assertions.assertNull(capturedEntity.getFirstAnalogCost());
        Assertions.assertEquals(notificationStepCost, capturedEntity.getSecondAnalogCost());
        Assertions.assertNull(capturedEntity.getIsRefusedCancelled());
    }

    @Test
    void getTotalCostTest() {
        // Given
        CostComponentsEntity entity = newCostComponentsEntity();
        entity.setBaseCost(100);
        entity.setSimpleRegisteredLetterCost(50);
        entity.setFirstAnalogCost(25);
        entity.setSecondAnalogCost(25);
        entity.setIsRefusedCancelled(false); // null is treated as false, too

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
        CostComponentsEntity entity = newCostComponentsEntity();
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
        CostComponentsEntity entity = newCostComponentsEntity();
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
        String creditorTaxId2 = "creditorTaxId2";
        String noticeCode2 = "noticeCode2";

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
    void getIuvsForIunAndRecIndex_NoResultsTest() {
        // Given
        when(costComponentsDao.getItems(pk)).thenReturn(Flux.empty());  // No results returned

        // When
        List<CostComponentsInt> resultList = costComponentService.getIuvsForIunAndRecIndex(iun, recIndex).collectList().block();

        // Then
        Assertions.assertNotNull(resultList, "Result list should not be null");
        Assertions.assertTrue(resultList.isEmpty(), "Result list should be empty");  // Assert that the result list is empty

        verify(costComponentsDao, times(1)).getItems(pk);
    }

    @Test
    void getIuvsForIunAndRecIndex_IllegalPkSkTest() {
        // Given
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

    @Test
    void getIuvsForIunAndRecIndex_NullPkSkTest() {
        // Given
        CostComponentsEntity entity1 = newCostComponentsEntity();
        entity1.setPk(null);
        entity1.setSk(null);

        when(costComponentsDao.getItems(pk)).thenReturn(Flux.just(entity1));

        // When
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            costComponentService.getIuvsForIunAndRecIndex(iun, recIndex).collectList().block();
        });
    }

    private CostComponentsEntity newCostComponentsEntity() {
        CostComponentsEntity entity = new CostComponentsEntity();

        // the pk and sk must be in the correct for, for the mapper not to fail
        entity.setPk(pk);
        entity.setSk(sk);

        entity.setBaseCost(0);
        entity.setSimpleRegisteredLetterCost(0);
        entity.setFirstAnalogCost(0);
        entity.setSecondAnalogCost(0);
        entity.setIsRefusedCancelled(false);
        return entity;
    }
}
