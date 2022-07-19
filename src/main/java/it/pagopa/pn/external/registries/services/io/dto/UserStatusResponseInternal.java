package it.pagopa.pn.external.registries.services.io.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserStatusResponseInternal {

    private String taxId;

    private List<String> preferredLanguages;

    public enum StatusEnum {
        APPIO_NOT_ACTIVE("APPIO_NOT_ACTIVE"),

        PN_NOT_ACTIVE("PN_NOT_ACTIVE"),

        PN_ACTIVE("PN_ACTIVE"),

        ERROR("ERROR");

        private String value;

        StatusEnum(String value) {
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

    private StatusEnum status;

}
