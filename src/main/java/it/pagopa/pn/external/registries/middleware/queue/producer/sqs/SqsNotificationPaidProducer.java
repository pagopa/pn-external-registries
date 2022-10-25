package it.pagopa.pn.external.registries.middleware.queue.producer.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsFifoMomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsNotificationPaidProducer extends AbstractSqsFifoMomProducer<PnExtRegistryNotificationPaidEvent> {

    public SqsNotificationPaidProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper ) {
        super(sqsClient, topic, objectMapper, PnExtRegistryNotificationPaidEvent.class );
    }
}
