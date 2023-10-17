package it.pagopa.pn.external.registries.dto.deliverypush;

import lombok.*;

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
    private String eventTimestamp;
    private String eventStorageTimestamp;
    private UpdateCostPhase updateCostPhase;
}
