package it.pagopa.pn.external.registries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCostResponseInt {
    private int recIndex;
    private String creditorTaxId;
    private String noticeCode;
    private CommunicationResultGroupInt result;
}
