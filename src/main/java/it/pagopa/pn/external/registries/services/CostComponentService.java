package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CostComponentService {
    @Autowired
    private CostComponentsDao costComponentsDao;

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
}
