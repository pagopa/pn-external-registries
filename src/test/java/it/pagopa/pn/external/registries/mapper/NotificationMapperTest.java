package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.NotificationRecipientV24;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.SentNotificationV25;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    @Test
    void externalToInternal_shouldMapFieldsAndRecipients() {
        SentNotificationV25 sentNotification = new SentNotificationV25();
        sentNotification.setSubject("subject");
        sentNotification.setSenderDenomination("denomination");

        NotificationRecipientV24 recipientV24 = new NotificationRecipientV24();
        recipientV24.setInternalId("id");
        sentNotification.setRecipients(Collections.singletonList(recipientV24));

        NotificationInt result = NotificationMapper.externalToInternal(sentNotification);

        assertEquals("subject", result.getSubject());
        assertEquals("denomination", result.getSenderDenomination());
        assertNotNull(result.getRecipients());
        assertEquals(1, result.getRecipients().size());
        assertEquals("id", result.getRecipients().get(0).getInternalId());
    }

    @Test
    void externalToInternal_shouldHandleNullRecipients() {
        SentNotificationV25 sentNotification = new SentNotificationV25();
        sentNotification.setSubject("subject");
        sentNotification.setSenderDenomination("denomination");
        sentNotification.setRecipients(null);

        NotificationInt result = NotificationMapper.externalToInternal(sentNotification);

        assertNull(result.getRecipients());
        assertEquals("subject", result.getSubject());
        assertEquals("denomination", result.getSenderDenomination());
    }
}




