package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.timelineservice.DeliveryInformationResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.DeliveryInformationResponse;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.ExtendedDeliveryMode;
import it.pagopa.pn.external.registries.services.bottomsheet.DeliveryModeInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimelineServiceMapperTest {

    @Test
    void externalToInternal_shouldMapAllFields() {
        Instant now = Instant.now();

        DeliveryInformationResponse ext = new DeliveryInformationResponse();
        ext.setRefinementOrViewedDate(now);
        ext.setSchedulingAnalogDate(now);
        ext.setDeliveryMode(ExtendedDeliveryMode.DIGITAL);
        ext.setIsNotificationCancelled(Boolean.FALSE);

        DeliveryInformationResponseInt result = TimelineServiceMapper.externalToInternal(ext);

        assertEquals(now, result.getRefinementOrViewedDate());
        assertEquals(now, result.getSchedulingAnalogDate());
        assertEquals(DeliveryModeInt.DIGITAL, result.getDeliveryMode());
        assertFalse(result.getIsNotificationCancelled());
    }

    @Test
    void externalToInternal_shouldHandleNullFields() {
        DeliveryInformationResponse ext = new DeliveryInformationResponse();
        ext.setRefinementOrViewedDate(null);
        ext.setSchedulingAnalogDate(null);
        ext.setDeliveryMode(null);
        ext.setIsNotificationCancelled(null);

        assertThrows(NullPointerException.class, () -> TimelineServiceMapper.externalToInternal(ext));
    }
}
