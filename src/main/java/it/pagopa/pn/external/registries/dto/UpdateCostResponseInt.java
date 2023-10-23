package it.pagopa.pn.external.registries.dto;

import lombok.*;

@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCostResponseInt {
    private int recIndex;
    private String creditorTaxId;
    private String noticeCode;
    private CommunicationResultGroupInt result;
}
