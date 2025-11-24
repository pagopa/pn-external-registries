package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.NotificationRecipientV24;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRecipientMapperTest {

    @Test
    void externalToInternal_shouldMapInternalId() {
        NotificationRecipientV24 recipientV24 = new NotificationRecipientV24();
        recipientV24.setInternalId("test-id");

        NotificationRecipientInt result = NotificationRecipientMapper.externalToInternal(recipientV24);

        assertNotNull(result);
        assertEquals("test-id", result.getInternalId());
    }

    @Test
    void externalToInternal_shouldReturnNullForNullInput() {
        NotificationRecipientInt result = NotificationRecipientMapper.externalToInternal(null);
        assertNull(result);
    }
}
