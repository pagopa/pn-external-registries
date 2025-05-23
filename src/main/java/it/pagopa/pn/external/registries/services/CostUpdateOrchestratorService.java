package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaymentForRecipientInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * @param vat vat to apply analog costs
     * @param notificationStepCost Cost of the single workflow step
     * @param iun The IUN of the notification
     * @param recIndex Recipient for which the cost update is requested
     * @param eventTimestamp Timestamp of the event
     * @param eventStorageTimestamp Timestamp of the event storage
     * @param updateCostPhase Phase of the cost update
     * @return A Flux of UpdateCostResponseInt with the update result for each IUV
     */
    public Flux<UpdateCostResponseInt> handleCostUpdateForIun(Integer vat, int notificationStepCost, String iun, int recIndex,
                                                              Instant eventTimestamp, Instant eventStorageTimestamp,
                                                              CostUpdateCostPhaseInt updateCostPhase) {
        if (iun == null || iun.trim().isEmpty()) {
            log.warn("IUN is null or empty. No cost update will be performed.");
            return Flux.empty();
        }

        log.info("Handling cost update for IUN: {}, recIndex: {}, notificationStepCost: {}, updateCostPhase: {}",
                iun, recIndex, notificationStepCost, updateCostPhase);

        // Retrieve IUVs using the provided iun and recIndex
        return costComponentService.getIuvsForIunAndRecIndex(iun, recIndex)
                .collectList() // collect the list of IUVs because I need to pass them all to handleCostUpdateForIuvs, accepting an array
                .flatMapMany(iuvs -> {
                    if (iuvs.isEmpty()) {
                        log.warn("No IUVs found for IUN: {} and recIndex: {}. No cost update will be performed.", iun, recIndex);
                        return Flux.empty();
                    }

                    PaymentForRecipientInt[] paymentsForRecipients = iuvs.stream()
                            .map(iuv -> new PaymentForRecipientInt(recIndex, iuv.getCreditorTaxId(), iuv.getNoticeCode()))
                            .toArray(PaymentForRecipientInt[]::new);

                    // the actual update
                    return handleCostUpdateForIuvs(vat, notificationStepCost, iun, paymentsForRecipients,
                            eventTimestamp, eventStorageTimestamp, updateCostPhase);
                })
                .doOnError(e -> log.error("An error occurred while processing IUVs for IUN: {} and recIndex: {}. Error: {}",
                            iun, recIndex, e.getMessage()));
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
    public Flux<UpdateCostResponseInt> handleCostUpdateForIuvs(Integer vat, int notificationStepCost, String iun,
                                              PaymentForRecipientInt[] paymentsForRecipients,
                                              Instant eventTimestamp, Instant eventStorageTimestamp,
                                                               CostUpdateCostPhaseInt updateCostPhase) {

        if (paymentsForRecipients == null || paymentsForRecipients.length == 0) {
            log.warn("PaymentsForRecipients is null or empty. No cost update will be performed.");
            return Flux.empty();
        }

        log.info("Updating the cost on GPD: iun: {}, notificationStepCost: {}, updateCostPhase: {}",
                iun, notificationStepCost, updateCostPhase);

        // the starting array
        return Flux.fromArray(paymentsForRecipients)
                .flatMap(paymentForRecipient ->
                        costComponentService.existCostItem(iun, paymentForRecipient.getRecIndex(),
                                        paymentForRecipient.getCreditorTaxId(), paymentForRecipient.getNoticeCode())
                                .flatMap(existCostItem -> {
                                    if(Boolean.TRUE.equals(existCostItem) || isNotSendCostPhase(updateCostPhase) ){
                                        log.info("Need to update cost item, costItemAlreadyExists={} updateCostPhase={} - : iun: {}, creditorTaxId={} noticeCode={}", existCostItem, updateCostPhase, iun, paymentForRecipient.getCreditorTaxId(), paymentForRecipient.getNoticeCode());
                                        //Solo se l'item UpdateCost esiste, dunque sono state verificate da deliveryPush le condizioni,
                                        // Ad esempio che la notifica non sia FLAT_RATE
                                        //Oppure se la updateCostPhase non è un invio analogico/invio di raccomandata semplice
                                        // Si procede con l'update dei costi verso GPD
                                        return updateNotificationCost(vat, notificationStepCost, iun, eventTimestamp, eventStorageTimestamp, updateCostPhase, paymentForRecipient);
                                    } else {
                                        log.warn("Skipped update for the cost on GPD - costItem not present : iun: {}, notificationStepCost: {}, updateCostPhase: {}",
                                                iun, notificationStepCost, updateCostPhase);
                                        UpdateCostResponseInt responseNoUpdate = new UpdateCostResponseInt();
                                        responseNoUpdate.setCreditorTaxId(paymentForRecipient.getCreditorTaxId());
                                        responseNoUpdate.setNoticeCode(paymentForRecipient.getNoticeCode());
                                        responseNoUpdate.setRecIndex(paymentForRecipient.getRecIndex());
                                        responseNoUpdate.setResult(CommunicationResultGroupInt.OK);

                                        return Mono.just(responseNoUpdate);
                                    }
                                })
                );
    }

    private boolean isNotSendCostPhase(CostUpdateCostPhaseInt updateCostPhase) {
        return switch (updateCostPhase){
            case VALIDATION, REQUEST_REFUSED, NOTIFICATION_CANCELLED -> true;
            default -> false;
        };
    }


    @NotNull
    private Mono<UpdateCostResponseInt> updateNotificationCost(
            Integer vat,
            int notificationStepCost, 
            String iun,
            Instant eventTimestamp, 
            Instant eventStorageTimestamp,
            CostUpdateCostPhaseInt updateCostPhase, 
            PaymentForRecipientInt paymentForRecipient
    ) {
        return costComponentService.insertStepCost(updateCostPhase, iun, paymentForRecipient.getRecIndex(),
                        paymentForRecipient.getCreditorTaxId(), paymentForRecipient.getNoticeCode(), notificationStepCost, vat)
                .doOnError(e -> log.error("An error occurred while inserting step cost for recIndex: {}. Error: {}",
                        paymentForRecipient.getRecIndex(), e.getMessage()))
                .flatMap(costComponent ->
                        costComponentService.getTotalCost(vat, iun, paymentForRecipient.getRecIndex(),
                                        paymentForRecipient.getCreditorTaxId(), paymentForRecipient.getNoticeCode())
                                .doOnError(e -> log.error("An error occurred while retrieving total cost for recIndex: {}. Error: {}",
                                        paymentForRecipient.getRecIndex(), e.getMessage()))
                )
                .flatMap(totalCost ->
                        updateCostService.updateCost(paymentForRecipient.getRecIndex(), iun,
                                paymentForRecipient.getCreditorTaxId(), paymentForRecipient.getNoticeCode(),
                                totalCost, updateCostPhase, eventTimestamp, eventStorageTimestamp)
                );
    }

}
