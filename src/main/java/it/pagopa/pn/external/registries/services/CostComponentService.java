package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import it.pagopa.pn.external.registries.util.CostUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

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

    public Mono<CostComponentsInt> insertStepCost(CostUpdateCostPhaseInt updateCostPhase, String iun, int recIndex,
                                                  String creditorTaxId, String noticeCode, Integer notificationStepCost, Integer vat) {
        final String insertString = "inserting cost components: pk={}, sk={}, iun={}, recIndex={}, creditorTaxId={}, noticeCode={}, notificationStepCost={}, updateCostPhase={}";
        final String updatingString = "updating cost components: pk={}, sk={}, iun={}, recIndex={}, creditorTaxId={}, noticeCode={}, notificationStepCost={}, updateCostPhase={}";

        // Validation of input parameters
        if (updateCostPhase == null || iun == null || recIndex < 0 ||
                creditorTaxId == null || noticeCode == null || notificationStepCost == null) {
            return Mono.error(new IllegalArgumentException("Input parameters should not be null"));
        }

        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        CostComponentsEntity entity = new CostComponentsEntity();
        entity.setPk(pk);
        entity.setSk(sk);

        // Setting cost fields to null to avoid updating them
        entity.setBaseCost(null);
        entity.setSimpleRegisteredLetterCost(null);
        entity.setFirstAnalogCost(null);
        entity.setSecondAnalogCost(null);
        entity.setIsRefusedCancelled(null);
        
        switch (updateCostPhase) {
            case VALIDATION:
                entity.setBaseCost(notificationStepCost);
                entity.setSimpleRegisteredLetterCost(0);
                entity.setFirstAnalogCost(0);
                entity.setSecondAnalogCost(0);
                entity.setIsRefusedCancelled(false);

                log.info(insertString,
                        pk, sk, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost, updateCostPhase);

                return costComponentsDao.insertOrUpdate(entity)
                        .map(costComponentsMapper::dbToInternal);

            case REQUEST_REFUSED, NOTIFICATION_CANCELLED:
                entity.setBaseCost(0);
                entity.setSimpleRegisteredLetterCost(0);
                entity.setFirstAnalogCost(0);
                entity.setSecondAnalogCost(0);
                entity.setIsRefusedCancelled(true);
            
                log.info(insertString,
                        pk, sk, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost, updateCostPhase);

                return costComponentsDao.insertOrUpdate(entity)
                        .map(costComponentsMapper::dbToInternal);

            case SEND_SIMPLE_REGISTERED_LETTER:
                entity.setSimpleRegisteredLetterCost(notificationStepCost);
                entity.setVat(vat);
                break;

            case SEND_ANALOG_DOMICILE_ATTEMPT_0:
                entity.setFirstAnalogCost(notificationStepCost);
                entity.setVat(vat);
                break;

            case SEND_ANALOG_DOMICILE_ATTEMPT_1:
                entity.setSecondAnalogCost(notificationStepCost);
                entity.setVat(vat);
                break;

            default:
                return Mono.error(new IllegalArgumentException("Invalid updateCostPhase: " + updateCostPhase));
        }

        // we arrive here only for SEND_SIMPLE_REGISTERED_LETTER, SEND_ANALOG_DOMICILE_ATTEMPT_0 and SEND_ANALOG_DOMICILE_ATTEMPT_1,
        // requiring an update of the entity
        log.info(updatingString,
                pk, sk, iun, recIndex, creditorTaxId, noticeCode, notificationStepCost, updateCostPhase);

        return costComponentsDao.updateNotNullIfExists(entity)
                .map(costComponentsMapper::dbToInternal);
    }

    /**
     * Get total cost for a given iun and recIndex
     * @param iun iun
     * @param recIndex recipient index
     * @return a Mono of Integer (the computed total cost)
     */
    public Mono<Integer> getTotalCost(Integer vat, String iun, int recIndex, String creditorTaxId, String noticeCode) {
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        log.info("Getting total cost: pk={}, sk={}, iun={}, recIndex={}, creditorTaxId={}, noticeCode={} vat={}",
                pk, sk, iun, recIndex, creditorTaxId, noticeCode, vat);

        return getItem(iun, recIndex, creditorTaxId, noticeCode)
                .map(entity -> {
                    if (Boolean.TRUE.equals(entity.getIsRefusedCancelled())) {
                        return 0;
                    } else {
                        // false or null
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
                        
                        Integer analogCost = simpleRegisteredLetterCost + firstAnalogCost + secondAnalogCost;
                        Integer analogCostWithVat = CostUtils.getCostWithVat(vat, analogCost);
                        Integer totalCost = baseCost + analogCostWithVat;
                        log.info("Get cost completed: analogCost={} analogCostWithVat={} totalCost={} - iun={}, recIndex={}, creditorTaxId={}, noticeCode={}",
                                analogCost, analogCostWithVat, totalCost, iun, recIndex, creditorTaxId, noticeCode);
                        return totalCost;
                    }
                });
    }

    private Mono<CostComponentsEntity> getItem(String iun, int recIndex, String creditorTaxId, String noticeCode) {
        String pk = iun + "##" + recIndex;
        String sk = creditorTaxId + "##" + noticeCode;

        log.info("Getting total cost: pk={}, sk={}, iun={}, recIndex={}, creditorTaxId={}, noticeCode={}",
                pk, sk, iun, recIndex, creditorTaxId, noticeCode);

        return costComponentsDao.getItem(pk, sk);
    }

    public Mono<Boolean> existCostItem(String iun, int recIndex, String creditorTaxId, String noticeCode) {
        return getItem(iun, recIndex, creditorTaxId, noticeCode).map(
                Objects::nonNull
        ).defaultIfEmpty(false);
    }

    /**
     * Get cost components for a given iun and recIndex
     * @param iun iun
     * @param recIndex recipient index
     * @return a Flux of CostComponentsInt
     */
    public Flux<CostComponentsInt> getIuvsForIunAndRecIndex(String iun, int recIndex) {
        String pk = iun + "##" + recIndex;

        log.info("Getting cost components: pk={}, iun={}, recIndex={}",
                pk, iun, recIndex);

        return costComponentsDao.getItems(pk)
                .map(costComponentsMapper::dbToInternal);
    }
}
