package it.pagopa.pn.external.registries.dto.deliverypush;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class UpdateNotificationCost {
    private String iun;
    private int recIndex;
    private int notificationStepCost;
    private Integer vat;
    private Instant eventTimestamp;
    private Instant eventStorageTimestamp;
    private AnalogUpdateCostPhase updateCostPhase;
}
