package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaymentForRecipientInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Service
@Slf4j
public class CostUpdateOrchestratorService {

    private final CostComponentService costComponentService;
    private final UpdateCostService updateCostService;

    @Autowired
    CostUpdateOrchestratorService(CostComponentService costComponentService, UpdateCostService updateCostService) {
        this.costComponentService = costComponentService;
        this.updateCostService = updateCostService;
    }

    /**
     * Handles the cost update for a specific IUN, retrieves IUVs and then calls
     * handleCostUpdateForIuvs to process the update.
     *
     * @param notificationStepCost Cost of the single workflow step
     * @param iun The IUN of the notification
     * @param recIndex Recipient for which the cost update is requested
     * @param eventTimestamp Timestamp of the event
     * @param eventStorageTimestamp Timestamp of the event storage
     * @param updateCostPhase Phase of the cost update
     * @return A Flux of UpdateCostResponseInt with the update result for each IUV
     */
    public Flux<UpdateCostResponseInt> handleCostUpdateForIun(int notificationStepCost, String iun, int recIndex,
                                                              Instant eventTimestamp, Instant eventStorageTimestamp,
                                                              CostUpdateCostPhaseInt updateCostPhase) {
        // Method implementation
        // ...

        return Flux.empty();
    }

    /**
     * Handles the cost update for IUVs. It updates the cost component,
     * retrieves the total cost, and updates the cost on GPD.
     *
     * @param notificationStepCost Cost of the single workflow step
     * @param iun The IUN of the notification
     * @param paymentsForRecipients An array of payment information for recipients
     * @param eventTimestamp Timestamp of the event
     * @param eventStorageTimestamp Timestamp of the event storage
     * @param updateCostPhase Phase of the cost update
     * @return A Flux of UpdateCostResponseInt with the update result for each IUV
     */
    public Flux<UpdateCostResponseInt> handleCostUpdateForIuvs(int notificationStepCost, String iun,
                                              PaymentForRecipientInt[] paymentsForRecipients,
                                              Instant eventTimestamp, Instant eventStorageTimestamp,
                                                               CostUpdateCostPhaseInt updateCostPhase) {
        // Method implementation
        // ...

        return Flux.empty();
    }
}
