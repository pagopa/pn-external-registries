package it.pagopa.pn.external.registries.middleware.db.mapper;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import org.springframework.stereotype.Component;

@Component
public class CostComponentsMapper {
    public CostComponentsInt dbToInternal(CostComponentsEntity entity) {
        CostComponentsInt costComponents = new CostComponentsInt();
        String[] pkParts = entity.getPk().split("##");
        String[] skParts = entity.getSk().split("##");

        costComponents.setIun(pkParts[0]);
        costComponents.setRecIndex(pkParts[1]);
        costComponents.setCreditorTaxId(skParts[0]);
        costComponents.setNoticeCode(skParts[1]);
        costComponents.setBaseCost(entity.getBaseCost());
        costComponents.setSimpleRegisteredLetterCost(entity.getSimpleRegisteredLetterCost());
        costComponents.setFirstAnalogCost(entity.getFirstAnalogCost());
        costComponents.setSecondAnalogCost(entity.getSecondAnalogCost());
        costComponents.setIsRefusedCancelled(entity.getIsRefusedCancelled());

        return costComponents;
    }
}
