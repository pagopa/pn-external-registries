package it.pagopa.pn.external.registries.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class PaymentInfoInt {
    private int recIndex;
    private String creditorTaxId;
    private String noticeCode;
}
