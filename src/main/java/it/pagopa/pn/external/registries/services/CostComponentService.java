package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
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
