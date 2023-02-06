package it.pagopa.pn.external.registries.middleware.queue.io.producer.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsFifoMomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryIOSentMessageEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsIOSentMessageProducer extends AbstractSqsFifoMomProducer<PnExtRegistryIOSentMessageEvent> {

    public SqsIOSentMessageProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper ) {
        super(sqsClient, topic, objectMapper, PnExtRegistryIOSentMessageEvent.class );
    }
}
