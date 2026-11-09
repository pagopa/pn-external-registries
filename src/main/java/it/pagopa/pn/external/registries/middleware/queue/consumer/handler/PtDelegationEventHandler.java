package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnPtDelegationPayload;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.external.registries.services.PtDelegationService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils.getEventId;
import static it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils.getSentTimestamp;
import static it.pagopa.pn.external.registries.middleware.queue.utils.ConsumerUtils.setMdc;

@Component
@AllArgsConstructor
@CustomLog
public class PtDelegationEventHandler {
    private static final String PUBLISHER_NAME = "pn-kafka-bridge";

    private PtDelegationService ptDelegationService;

    @SqsListener(value = "${pn.external-registry.topics.kafka-bridge-delegations}")
    public void pnKafkaBridgePtDelegationEventConsumer(Message<PnPtDelegationPayload> message) {
        final String processName = "SAVE PT DELEGATION EVENT";
        setMdc(message);
        try {
            log.debug("Handle message from {} with content {}", PUBLISHER_NAME, message);
            HandleEventUtils.addCorrelationIdToMdc(getEventId(message));

            log.logStartingProcess(processName);
            ptDelegationService.saveDelegationEvent(message.getPayload(), getSentTimestamp(message)).block();
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
