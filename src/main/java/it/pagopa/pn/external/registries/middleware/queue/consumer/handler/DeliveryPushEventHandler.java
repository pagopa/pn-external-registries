package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.dto.deliverypush.UpdateNotificationCost;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_UPDATE_COST_FAILED;
import static it.pagopa.pn.external.registries.middleware.queue.consumer.handler.utils.HandleEventUtils.getEventId;
import static it.pagopa.pn.external.registries.middleware.queue.utils.ConsumerUtils.setMdc;

@Component
@AllArgsConstructor
@CustomLog
public class DeliveryPushEventHandler {
    private static final String PUBLISHER_NAME = PnLogger.EXTERNAL_SERVICES.PN_DELIVERY_PUSH;

    private CostUpdateOrchestratorService costUpdateOrchestratorService;

    @SqsListener(value = "${pn.external-registry.topics.delivery-push-input}")
    public void pnDeliveryPushUpdateCostEventConsumer(Message<UpdateNotificationCost> message) {
        final String processName = "UPDATE NOTIFICATION COST";
        setMdc(message);
        try {
            log.debug("Handle message from {} with content {}", PUBLISHER_NAME, message);
            String eventId = getEventId(message);

            UpdateNotificationCost updateNotificationCost = message.getPayload();
            HandleEventUtils.addIunAndRecIndexAndCorrIdToMdc(updateNotificationCost.getIun(), updateNotificationCost.getRecIndex(), eventId);

            log.logStartingProcess(processName);

            List<UpdateCostResponseInt> listResponse = costUpdateOrchestratorService.handleCostUpdateForIun(
                    updateNotificationCost.getVat(),
                    updateNotificationCost.getNotificationStepCost(),
                    updateNotificationCost.getIun(),
                    updateNotificationCost.getRecIndex(),
                    updateNotificationCost.getEventTimestamp(),
                    updateNotificationCost.getEventStorageTimestamp(),
                    CostUpdateCostPhaseInt.valueOf(updateNotificationCost.getUpdateCostPhase().getValue())
            ).collectList().block();

            if (listResponse != null) {
                listResponse.forEach(result -> {
                            switch (result.getResult()) {
                                case OK -> log.info("Update cost {} in phase={} successfully completed - iun={} recIndex={}",
                                        updateNotificationCost.getNotificationStepCost(), updateNotificationCost.getUpdateCostPhase(),
                                        updateNotificationCost.getIun(), updateNotificationCost.getRecIndex());
                                case KO ->
                                        log.warn("Cannot update cost {} in phase={} for an error, the update will not be retried. - iun={} recIndex={}",
                                                updateNotificationCost.getNotificationStepCost(), updateNotificationCost.getUpdateCostPhase(),
                                                updateNotificationCost.getIun(), updateNotificationCost.getRecIndex());
                                case RETRY -> {
                                    log.info("Cannot update cost {} in phase={}, the update will be retried - iun={} recIndex={}",
                                            updateNotificationCost.getNotificationStepCost(), updateNotificationCost.getUpdateCostPhase(),
                                            updateNotificationCost.getIun(), updateNotificationCost.getRecIndex());
                                    throw new PnInternalException("cannot update cost", ERROR_CODE_EXTERNALREGISTRIES_UPDATE_COST_FAILED);
                                }
                            }
                        }
                );
            }
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage());
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}