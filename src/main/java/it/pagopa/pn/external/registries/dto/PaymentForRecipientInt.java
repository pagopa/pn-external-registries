package it.pagopa.pn.external.registries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PaymentForRecipientInt {
    private int recIndex;
    private String creditorTaxId;
    private String noticeCode;
}
