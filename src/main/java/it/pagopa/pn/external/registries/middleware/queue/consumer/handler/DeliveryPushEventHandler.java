package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.external.registries.dto.deliverypush.UpdateNotificationCost;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils.getEventId;

@Configuration
@AllArgsConstructor
@CustomLog
public class DeliveryPushEventHandler {
    private static final String PUBLISHER_NAME = PnLogger.EXTERNAL_SERVICES.PN_DELIVERY_PUSH;
    
    private CostUpdateOrchestratorService costUpdateOrchestratorService;
    
    @Bean
    public Consumer<Message<UpdateNotificationCost>> pnDeliveryPushUpdateCostEventConsumer() {
        final String processName = "UPDATE NOTIFICATION COST";
        
        return message -> {
            try {
                log.debug("Handle message from {} with content {}", PUBLISHER_NAME, message);
                String eventId = getEventId(message);

                UpdateNotificationCost updateNotificationCost = message.getPayload();
                HandleEventUtils.addIunAndRecIndexAndCorrIdToMdc(updateNotificationCost.getIun(), updateNotificationCost.getRecIndex(), eventId);

                log.logStartingProcess(processName);

                costUpdateOrchestratorService.handleCostUpdate();
                
                log.logEndingProcess(processName);
            } catch (Exception ex) {
                log.logEndingProcess(processName, false, ex.getMessage());
                HandleEventUtils.handleException(message.getHeaders(), ex);
                throw ex;
            }
        };
    }
}