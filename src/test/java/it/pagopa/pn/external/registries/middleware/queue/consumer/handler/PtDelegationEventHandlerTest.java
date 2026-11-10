package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.services.PtDelegationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;

@ExtendWith(SpringExtension.class)
class PtDelegationEventHandlerTest {
    private static final Instant SENT_AT = Instant.parse("2026-11-03T11:59:58.250Z");

    @InjectMocks
    private PtDelegationEventHandler ptDelegationEventHandler;
    @Mock
    private PtDelegationService ptDelegationService;

    @Test
    void pnKafkaBridgePtDelegationEventConsumer() {
        Message<PnPtDelegationPayload> message = getMessage();
        Mockito.when(ptDelegationService.saveDelegationEvent(message.getPayload(), SENT_AT)).thenReturn(Mono.just(Boolean.TRUE));

        Assertions.assertDoesNotThrow(() -> ptDelegationEventHandler.pnKafkaBridgePtDelegationEventConsumer(message));
        Mockito.verify(ptDelegationService).saveDelegationEvent(message.getPayload(), SENT_AT);
    }

    @Test
    void pnKafkaBridgePtDelegationEventConsumerError() {
        Message<PnPtDelegationPayload> message = getMessage();
        Mockito.when(ptDelegationService.saveDelegationEvent(message.getPayload(), SENT_AT))
                .thenReturn(Mono.error(new PnInternalException("error", "PN_EXTERNALREGISTRIES_PT_DELEGATION_INSTITUTION_NOT_ONBOARDED")));

        Assertions.assertThrows(PnInternalException.class, () -> ptDelegationEventHandler.pnKafkaBridgePtDelegationEventConsumer(message));
    }

    private static Message<PnPtDelegationPayload> getMessage() {
        PnPtDelegationPayload payload = new PnPtDelegationPayload();
        payload.setDelegationId("delegation-1");
        payload.setInstitutionId("ente-A");
        payload.setPtId("pt-1");
        return MessageBuilder.withPayload(payload)
                .setHeader("eventId", "delegation-1_source-event-1")
                .setHeader("iun", "delegation-1")
                .setHeader("Sqs_Msa_SentTimestamp", String.valueOf(SENT_AT.toEpochMilli()))
                .build();
    }
}
