package it.pagopa.pn.external.registries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
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
