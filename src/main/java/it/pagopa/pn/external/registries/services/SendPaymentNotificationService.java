package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.api.dto.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Service
@Slf4j
public class SendPaymentNotificationService {
    private final MomProducer<PnExtRegistryNotificationPaidEvent> notificationPaidProducer;

    public SendPaymentNotificationService(MomProducer<PnExtRegistryNotificationPaidEvent> notificationPaidProducer) { 
        this.notificationPaidProducer = notificationPaidProducer;
    }

    public Mono<Void> sendPaymentNotification(String paTaxId, String noticeCode){
        return Mono.fromRunnable(() -> {
            log.info( "Send payment event for paTaxId={} noticeCode={}", paTaxId, noticeCode );
            PnExtRegistryNotificationPaidEvent event = buildNotificationPaid( paTaxId, noticeCode );

            notificationPaidProducer.push(event);
        });
    }

    private PnExtRegistryNotificationPaidEvent buildNotificationPaid( String paTaxId, String noticeCode ) {
        Instant eventDate = Instant.now();
        String eventId = paTaxId + "_notification_paid_" + noticeCode;
        return PnExtRegistryNotificationPaidEvent.builder()
                .messageDeduplicationId(eventId)
                .messageGroupId("delivery")
                .header( StandardEventHeader.builder()
                        .iun( paTaxId ) //TODO non c'è lo iun capire se obbligatorio
                        .eventId( eventId )
                        .createdAt( eventDate )
                        .eventType( EventType.NOTIFICATION_PAID.name() )
                        .publisher( EventPublisher.EXTERNAL_REGISTRY.name())
                        .build()
                )
                .payload(
                        PnExtRegistryNotificationPaidEvent.Payload.builder()
                                .eventDate(eventDate)
                                .noticeCode(noticeCode)
                                .paTaxId(paTaxId)
                                .build()
                )
                .build();
    }
    //MONO OBJECT , .then(new Object())
}
