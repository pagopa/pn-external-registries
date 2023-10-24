package it.pagopa.pn.external.registries.dto.deliverypush;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum AnalogUpdateCostPhase {
    SEND_ANALOG_DOMICILE_ATTEMPT_0(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_0.getValue()),

    SEND_ANALOG_DOMICILE_ATTEMPT_1(CostUpdateCostPhaseInt.SEND_ANALOG_DOMICILE_ATTEMPT_1.getValue()),

    SEND_SIMPLE_REGISTERED_LETTER(CostUpdateCostPhaseInt.SEND_SIMPLE_REGISTERED_LETTER.getValue());

    private final String value;

    AnalogUpdateCostPhase(String value) {
        this.value = value;
    }

}
