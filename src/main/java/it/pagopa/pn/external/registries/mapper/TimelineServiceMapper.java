package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.timelineservice.DeliveryInformationResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.DeliveryInformationResponse;
import it.pagopa.pn.external.registries.services.bottomsheet.ExtendedDeliveryMode;

public class TimelineServiceMapper {

    private TimelineServiceMapper() {

    }

    public static DeliveryInformationResponseInt externalToInternal (DeliveryInformationResponse deliveryInformationResponse) {

        return DeliveryInformationResponseInt.builder()
                .refinementOrViewedDate(deliveryInformationResponse.getRefinementOrViewedDate())
                .schedulingAnalogDate(deliveryInformationResponse.getSchedulingAnalogDate())
                .deliveryMode(ExtendedDeliveryMode.valueOf(deliveryInformationResponse.getDeliveryMode().getValue()))
                .isNotificationCancelled(deliveryInformationResponse.getIsNotificationCancelled())
                .build();
    }
}
