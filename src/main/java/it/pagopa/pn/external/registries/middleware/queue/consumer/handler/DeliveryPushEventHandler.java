package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.external.registries.dto.deliverypush.UpdateNotificationCost;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@CustomLog
public class DeliveryPushEventHandler {
    private static final String PUBLISHER_NAME = PnLogger.EXTERNAL_SERVICES.PN_DELIVERY_PUSH;
    
    @Bean
    public Consumer<Message<UpdateNotificationCost>> pnDeliveryPushUpdateCostEventConsumer() {
        return message -> {
            try {
                log.debug("Handle message from {} with content {}", PUBLISHER_NAME, message);

            } catch (Exception ex) {
                HandleEventUtils.handleException(message.getHeaders(), ex);
                throw ex;
            }

        };
    }
}