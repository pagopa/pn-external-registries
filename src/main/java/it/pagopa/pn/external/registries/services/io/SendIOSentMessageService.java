package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.api.dto.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class SendIOSentMessageService {
    private final MomProducer<PnExtRegistryIOSentMessageEvent> iosentmessageProducer;

    public SendIOSentMessageService(MomProducer<PnExtRegistryIOSentMessageEvent> notificationPaidProducer) {
        this.iosentmessageProducer = notificationPaidProducer;
    }

    public Mono<Void> sendIOSentMessageNotification(String iun, int recIndex, String internalId, Instant sentDate){
        return Mono.fromRunnable(() -> {
            log.info( "Send IONotificationMessage event for iun={} recIndex={} internalId={} sentDate={}", iun, recIndex, internalId, sentDate );
            PnExtRegistryIOSentMessageEvent event = buildIOSentMessageEvent( iun, recIndex, internalId, sentDate );

            iosentmessageProducer.push(event);
            log.info( "Sent IONotificationMessage  event for iun={} recIndex={}", iun, recIndex);
        });
    }

    private PnExtRegistryIOSentMessageEvent buildIOSentMessageEvent(String iun, int recIndex, String internalId, Instant sentDate ) {
        Instant eventDate = Instant.now();
        String eventId = iun + "_iosentmessage_" + recIndex;
        return PnExtRegistryIOSentMessageEvent.builder()
                .messageDeduplicationId(eventId)
                .messageGroupId("external-registry")
                .header( StandardEventHeader.builder()
                        .iun( iun )
                        .eventId( eventId )
                        .createdAt( eventDate )
                        .eventType( EventType.SEND_IO_MESSAGE_REQUEST.name() )
                        .publisher( EventPublisher.EXTERNAL_REGISTRY.name())
                        .build()
                )
                .payload(
                        PnExtRegistryIOSentMessageEvent.Payload.builder()
                                .sendDate(sentDate)
                                .iun(iun)
                                .recIndex(recIndex)
                                .internalId (internalId)
                                .build()
                )
                .build();
    }
}
