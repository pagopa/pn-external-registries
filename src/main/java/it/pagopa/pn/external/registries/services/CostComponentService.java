package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CostComponentService {
    private CostComponentsDao costComponentsDao;

    private CostComponentsMapper costComponentsMapper;

    @Autowired
    CostComponentService(CostComponentsDao costComponentsDao, CostComponentsMapper costComponentsMapper) {
        this.costComponentsDao = costComponentsDao;
        this.costComponentsMapper = costComponentsMapper;
    }

    public Mono<CostComponentsInt> insertStepCost(String updateCostPhase, String iun, String recIndex,
                                                  String creditorTaxId, String noticeCode, Integer notificationStepCost) {
        // Validation of input parameters
        if (updateCostPhase == null || iun == null || recIndex == null ||
                creditorTaxId == null || noticeCode == null || notificationStepCost == null) {
            return Mono.error(new IllegalArgumentException("Input parameters should not be null"));
        }

        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        CostComponentsEntity entity = new CostComponentsEntity();
        entity.setPk(pk);
        entity.setSk(sk);

        // Setting cost fields to null for avoid updating them
        entity.setBaseCost(null);
        entity.setSimpleRegisteredLetterCost(null);
        entity.setFirstAnalogCost(null);
        entity.setSecondAnalogCost(null);
        entity.setIsRefusedCancelled(null);

        switch (updateCostPhase) {
            case "VALIDATION":
                entity.setBaseCost(notificationStepCost);
                entity.setSimpleRegisteredLetterCost(0);
                entity.setFirstAnalogCost(0);
                entity.setSecondAnalogCost(0);
                entity.setIsRefusedCancelled(false);

                return costComponentsDao.insertOrUpdate(entity)
                        .map(costComponentsMapper::dbToInternal);

            case "REQUEST_REFUSED", "NOTIFICATION_CANCELLED":
                entity.setIsRefusedCancelled(true);
                entity.setBaseCost(0);
                entity.setSimpleRegisteredLetterCost(0);
                entity.setFirstAnalogCost(0);
                entity.setSecondAnalogCost(0);

                return costComponentsDao.insertOrUpdate(entity)
                        .map(costComponentsMapper::dbToInternal);

            case "SEND_SIMPLE_REGISTERED_LETTER":
                // all other fields to null, for leaving them unchanged
                entity.setSimpleRegisteredLetterCost(notificationStepCost);

                return costComponentsDao.updateNotNull(entity)
                        .map(costComponentsMapper::dbToInternal);

            case "SEND_ANALOG_DOMICILE_ATTEMPT_0":
                // all other fields to null, for leaving them unchanged
                entity.setFirstAnalogCost(notificationStepCost);

                return costComponentsDao.updateNotNull(entity)
                        .map(costComponentsMapper::dbToInternal);

            case "SEND_ANALOG_DOMICILE_ATTEMPT_1":
                // all other fields to null, for leaving them unchanged
                entity.setSecondAnalogCost(notificationStepCost);

                return costComponentsDao.updateNotNull(entity)
                        .map(costComponentsMapper::dbToInternal);

            default:
                return Mono.error(new IllegalArgumentException("Invalid updateCostPhase"));
        }
    }

    /**
     * Get total cost for a given iun and recIndex
     * @param iun iun
     * @param recIndex recipient index
     * @return a Mono of Integer (the computed total cost)
     */
    public Mono<Integer> getTotalCost(String iun, String recIndex, String creditorTaxId, String noticeCode) {
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        log.info("getting total cost: pk={}, sk={}, iun={}, recIndex={}, creditorTaxId={}, noticeCode={}",
                pk, sk, iun, recIndex, creditorTaxId, noticeCode);

        return costComponentsDao.getItem(pk, sk)
                .map(entity -> {
                    if (Boolean.TRUE.equals(entity.getIsRefusedCancelled())) {
                        return 0;
                    } else {
                        var baseCost = entity.getBaseCost();
                        var simpleRegisteredLetterCost = entity.getSimpleRegisteredLetterCost();
                        var firstAnalogCost = entity.getFirstAnalogCost();
                        var secondAnalogCost = entity.getSecondAnalogCost();

                        // fix in case of null values
                        if (baseCost == null) {
                            baseCost = 0;
                        }
                        if (simpleRegisteredLetterCost == null) {
                            simpleRegisteredLetterCost = 0;
                        }
                        if (firstAnalogCost == null) {
                            firstAnalogCost = 0;
                        }
                        if (secondAnalogCost == null) {
                            secondAnalogCost = 0;
                        }

                        return baseCost + simpleRegisteredLetterCost + firstAnalogCost + secondAnalogCost;
                    }
                });
    }

    /**
     * Get cost components for a given iun and recIndex
     * @param iun iun
     * @param recIndex recipient index
     * @return a Flux of CostComponentsInt
     */
    public Flux<CostComponentsInt> getIuvsForIunAndRecIndex(String iun, String recIndex) {
        String pk = iun + "##" + recIndex;
        return costComponentsDao.getItems(pk)
                .map(costComponentsMapper::dbToInternal);
    }
}
