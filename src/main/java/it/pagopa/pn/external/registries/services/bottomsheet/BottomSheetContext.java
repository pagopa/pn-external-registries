package it.pagopa.pn.external.registries.services.bottomsheet;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder(toBuilder = true)
@Getter
public class BottomSheetContext {
    private Instant schedulingAnalogDate;
    private Instant refinementOrViewDate;
    private ExtendedDeliveryMode deliveryMode;
    private boolean isCancelled;
    private String iun;
    private String senderDenomination;
    private String subject;
}
