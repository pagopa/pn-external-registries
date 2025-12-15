package it.pagopa.pn.external.registries;

import it.pagopa.pn.external.registries.middleware.queue.io.producer.sqs.SqsIOSentMessageProducer;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class MockProducerTest {
    @MockitoBean
    protected SqsIOSentMessageProducer sqsIOSentMessageProducer;

    @MockitoBean
    protected SqsNotificationPaidProducer sqsNotificationPaidProducer;
}
