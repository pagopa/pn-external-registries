package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.NotificationRecipientV24;

public class NotificationRecipientMapper {

    private NotificationRecipientMapper() {

    }

    public static NotificationRecipientInt externalToInternal(NotificationRecipientV24 recipient) {
        if (recipient == null) {
            return null;
        }

        return NotificationRecipientInt.builder()
                .internalId(recipient.getInternalId())
                .build();
    }
}
