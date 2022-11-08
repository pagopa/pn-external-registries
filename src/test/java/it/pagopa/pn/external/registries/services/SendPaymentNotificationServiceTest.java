package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SendPaymentNotificationServiceTest {

    private SendPaymentNotificationService service;

    @BeforeAll
    public void setup() {
        @SuppressWarnings("unchecked")
        MomProducer<PnExtRegistryNotificationPaidEvent> notificationPaidProducer = (MomProducer<PnExtRegistryNotificationPaidEvent>) Mockito.mock(MomProducer.class);
        this.service = new SendPaymentNotificationService(notificationPaidProducer);
    }

    @Test
    void sendPaymentNotification() {
        // GIVEN
        String taxId = "mocked-taxId";
        String noticeCode = "mocked-noticeCode";

        // THEN
        Assertions.assertDoesNotThrow(() -> service.sendPaymentNotification(taxId, noticeCode).block());
    }

}
