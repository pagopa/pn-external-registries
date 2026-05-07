package it.pagopa.pn.external.registries.dto;

import lombok.*;

@Getter
@ToString
public enum AnalogUpdateCostPhaseInt {
    SEND_ANALOG_DOMICILE_ATTEMPT_0("SEND_ANALOG_DOMICILE_ATTEMPT_0"),
    SEND_ANALOG_DOMICILE_ATTEMPT_1("SEND_ANALOG_DOMICILE_ATTEMPT_1");

    private final String value;

    AnalogUpdateCostPhaseInt(String value) {
        this.value = value;
    }
}
