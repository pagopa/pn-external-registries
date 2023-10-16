package it.pagopa.pn.external.registries.dto;

import lombok.Getter;

@Getter
public enum CostUpdateResultResponseInt {
    OK_UPDATED("OK_UPDATED"),
    OK_IN_PAYMENT("OK_IN_PAYMENT"),
    SEND_ANALOG_DOMICILE_ATTEMPT_1("KO_NOT_FOUND"),
    KO_CANNOT_UPDATE("KO_CANNOT_UPDATE"),
    KO_RETRY("KO_RETRY");

    private final String value;

    CostUpdateResultResponseInt(String value) {
        this.value = value;
    }

    public static CostUpdateResultResponseInt fromValue(String value) {
        for (CostUpdateResultResponseInt phase : CostUpdateResultResponseInt.values()) {
            if (phase.getValue().equalsIgnoreCase(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
