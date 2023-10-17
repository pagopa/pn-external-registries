package it.pagopa.pn.external.registries.dto.deliverypush;

public enum UpdateCostPhase {
    SEND_ANALOG_DOMICILE_ATTEMPT_0("SEND_ANALOG_DOMICILE_ATTEMPT_0"),

    SEND_ANALOG_DOMICILE_ATTEMPT_1("SEND_ANALOG_DOMICILE_ATTEMPT_1"),

    SEND_SIMPLE_REGISTERED_LETTER("SEND_SIMPLE_REGISTERED_LETTER");

    private final String value;

    UpdateCostPhase(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
