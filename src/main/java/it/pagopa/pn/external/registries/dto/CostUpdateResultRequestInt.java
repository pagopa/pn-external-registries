package it.pagopa.pn.external.registries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode
public class CostUpdateResultRequestInt {
    private String creditorTaxId;
    private String noticeCode;
    private CostUpdateCostPhaseInt updateCostPhase;
    private String requestId;
    private int statusCode;
    private String jsonResponse;
    private int notificationCost;
    private String iun;
    private Instant eventTimestamp;
    private Instant eventStorageTimestamp;
    private Instant communicationTimestamp;
}
