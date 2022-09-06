package it.pagopa.pn.external.registries.middleware.queue.producer.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsNotificationPaidProducer extends AbstractSqsMomProducer<PnExtRegistryNotificationPaidEvent> {

    public SqsNotificationPaidProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper ) {
        super(sqsClient, topic, objectMapper, PnExtRegistryNotificationPaidEvent.class );
    }
}
