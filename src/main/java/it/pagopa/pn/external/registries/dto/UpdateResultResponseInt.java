package it.pagopa.pn.external.registries.dto;

import lombok.Getter;

@Getter
public enum UpdateResultResponseInt {
    OK_UPDATED("OK_UPDATED"),
    OK_IN_PAYMENT("OK_IN_PAYMENT"),
    SEND_ANALOG_DOMICILE_ATTEMPT_1("KO_NOT_FOUND"),
    KO_CANNOT_UPDATE("KO_CANNOT_UPDATE"),
    KO_RETRY("KO_RETRY");

    private final String value;

    UpdateResultResponseInt(String value) {
        this.value = value;
    }

    public static UpdateResultResponseInt fromValue(String value) {
        for (UpdateResultResponseInt phase : UpdateResultResponseInt.values()) {
            if (phase.getValue().equalsIgnoreCase(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
