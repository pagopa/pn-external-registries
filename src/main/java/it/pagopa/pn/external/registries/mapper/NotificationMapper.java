package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.NotificationRecipientV24;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.SentNotificationV25;

import java.util.List;

public class NotificationMapper {

    public static NotificationInt externalToInternal(SentNotificationV25 sentNotification) {
        List<NotificationRecipientInt> listNotificationRecipientInt = mapNotificationRecipient(sentNotification.getRecipients());
        return NotificationInt.builder()
                .subject(sentNotification.getSubject())
                .senderDenomination(sentNotification.getSenderDenomination())
                .recipients(listNotificationRecipientInt)
                .build();
    }

    private static List<NotificationRecipientInt> mapNotificationRecipient(List<NotificationRecipientV24> recipients) {
        if (recipients == null) {
            return null;
        }

        return recipients
                .stream()
                .map(NotificationRecipientMapper::externalToInternal)
                .toList();
    }
}
