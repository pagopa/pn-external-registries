package it.pagopa.pn.external.registries.dto;

import lombok.Getter;

@Getter
public enum UpdateCostPhaseInt {
    VALIDATION("VALIDATION"),
    SEND_ANALOG_DOMICILE_ATTEMPT_0("SEND_ANALOG_DOMICILE_ATTEMPT_0"),
    SEND_ANALOG_DOMICILE_ATTEMPT_1("SEND_ANALOG_DOMICILE_ATTEMPT_1"),
    SEND_SIMPLE_REGISTERED_LETTER("SEND_SIMPLE_REGISTERED_LETTER"),
    REQUEST_REFUSED("REQUEST_REFUSED"),
    NOTIFICATION_CANCELLED("NOTIFICATION_CANCELLED");

    private final String value;

    UpdateCostPhaseInt(String value) {
        this.value = value;
    }

    public static UpdateCostPhaseInt fromValue(String value) {
        for (UpdateCostPhaseInt phase : UpdateCostPhaseInt.values()) {
            if (phase.getValue().equalsIgnoreCase(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
