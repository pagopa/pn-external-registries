package it.pagopa.pn.external.registries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class CostComponentsInt {
    private String iun;
    private String recIndex;
    private String creditorTaxId;
    private String noticeCode;
    private Integer baseCost = 0;
    private Integer simpleRegisteredLetterCost = 0;
    private Integer firstAnalogCost = 0;
    private Integer secondAnalogCost = 0;
    private Boolean isRefusedCancelled = false;
}
