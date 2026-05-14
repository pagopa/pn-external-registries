package it.pagopa.pn.external.registries.dto;

public enum UpdateCostOptionEnum {
    CHECK_RESPONSE_STATUSES(true),
    NO_CHECK_RESPONSE_STATUSES(false);

    private final boolean checkResponseStatuses;

    UpdateCostOptionEnum(boolean checkResponseStatuses) {
        this.checkResponseStatuses = checkResponseStatuses;
    }

     public boolean isCheckResponseStatuses() {
        return checkResponseStatuses;
    }
}
