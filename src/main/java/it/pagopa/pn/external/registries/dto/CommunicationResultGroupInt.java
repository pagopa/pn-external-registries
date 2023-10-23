package it.pagopa.pn.external.registries.dto;

import lombok.Getter;

@Getter
public enum CommunicationResultGroupInt {
    OK("OK"),
    KO("KO"),
    RETRY("RETRY");

    private final String value;

    CommunicationResultGroupInt(String value) {
        this.value = value;
    }
}
