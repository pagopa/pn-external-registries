package it.pagopa.pn.external.registries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateCostResponseInt {
    private int recIndex;
    private String creditorTaxId;
    private String noticeCode;
    private CommunicationResultGroupInt result;
}
