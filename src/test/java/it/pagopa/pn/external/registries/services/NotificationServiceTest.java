package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.dto.delivery.NotificationRecipientInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.NotificationRecipientV24;
import it.pagopa.pn.external.registries.generated.openapi.msclient.delivery.v1.dto.SentNotificationV25;
import it.pagopa.pn.external.registries.middleware.msclient.DeliveryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;


class NotificationServiceTest {
    private DeliveryClient deliveryClient;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        deliveryClient = Mockito.mock(DeliveryClient.class);
        notificationService = new NotificationService(deliveryClient);
    }

    @Test
    void getSentNotificationPrivate_shouldMapAndReturnNotificationInt() {
        String iun = "test-iun";

        NotificationRecipientV24 recipientV24 = new NotificationRecipientV24();
        recipientV24.setInternalId("id");

        SentNotificationV25 ext = new SentNotificationV25();
        ext.setSenderDenomination("denomination");
        ext.setSubject("subject");
        ext.setRecipients(List.of(recipientV24));

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .internalId("id")
                .build();

        List<NotificationRecipientInt> recipients = new ArrayList<>();
        recipients.add(recipient);

        NotificationInt expected = NotificationInt.builder()
                .senderDenomination("denomination")
                .subject("subject")
                .recipients(recipients)
                .build();

        Mockito.when(deliveryClient.getSentNotificationPrivate(iun))
                .thenReturn(Mono.just(ext));

        StepVerifier.create(notificationService.getSentNotificationPrivate(iun))
                .expectNext(expected)
                .verifyComplete();
    }

}