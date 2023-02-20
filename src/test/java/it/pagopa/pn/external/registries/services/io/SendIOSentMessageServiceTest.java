package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryIOSentMessageEvent;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import it.pagopa.pn.external.registries.services.SendPaymentNotificationService;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendIOSentMessageServiceTest {

    private SendIOSentMessageService service;
    private final MomProducer<PnExtRegistryIOSentMessageEvent> ioSentMessageEventMomProducer = (MomProducer<PnExtRegistryIOSentMessageEvent>) Mockito.mock(MomProducer.class);

    @BeforeAll
    public void setup() {
        this.service = new SendIOSentMessageService(ioSentMessageEventMomProducer);
    }

    @Test
    void sendIOSentMessageNotification() {
        // GIVEN
        String iun = "IUN-123";
        String internalId="PF-123456789";
        int recIndex = 0;
        Instant sent = Instant.now();

        // WHEN
        Assertions.assertDoesNotThrow(() -> service.sendIOSentMessageNotification(iun, recIndex, internalId, sent).block());

        // THEN
        Mockito.verify(ioSentMessageEventMomProducer).push(Mockito.any(PnExtRegistryIOSentMessageEvent.class));
    }
}