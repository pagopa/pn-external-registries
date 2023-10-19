package it.pagopa.pn.external.registries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UpdateCostResponseInt {
    private String recIndex;
    private String creditorTaxId;
    private String noticeCode;
    private String result;
}
