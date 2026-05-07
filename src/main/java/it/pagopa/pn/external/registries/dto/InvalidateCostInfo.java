package it.pagopa.pn.external.registries.dto;

import lombok.Data;

@Data
public class InvalidateCostInfo {
    private Integer totalCost;
    private PaymentForRecipientInt payment;
}
