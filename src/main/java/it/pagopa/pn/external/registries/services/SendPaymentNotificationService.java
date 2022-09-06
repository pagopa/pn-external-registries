package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.commons.abstractions.MomProducer;
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

    public Mono<Void> sendPaymentNotification(String paTaxId, String noticeCode, Instant eventDate){
        PnExtRegistryNotificationPaidEvent event = buildNotificationPaid( paTaxId, noticeCode, eventDate );
        return Mono.fromRunnable(() -> notificationPaidProducer.push(event));
    }

    private PnExtRegistryNotificationPaidEvent buildNotificationPaid( String paTaxId, String noticeCode, Instant eventDate ) {
        return PnExtRegistryNotificationPaidEvent.builder()
                .header( StandardEventHeader.builder()
                        .iun( paTaxId ) //TODO non c'è lo iun capire se obbligatorio
                        .eventId( paTaxId + "_notification_paid" + noticeCode +"_eventDate"+eventDate )
                        .createdAt( Instant.now() )
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
