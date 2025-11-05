package it.pagopa.pn.external.registries.dto.timelineservice;

import it.pagopa.pn.external.registries.services.bottomsheet.ExtendedDeliveryMode;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class DeliveryInformationResponseInt {
    private ExtendedDeliveryMode deliveryMode;
    private Instant refinementOrViewedDate;
    private Instant schedulingAnalogDate;
    private Boolean isNotificationCancelled;
}
