package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@AllArgsConstructor
public class PaperCostService {

    private final CostComponentService costComponentService;
    private final UpdateCostService updateCostService;
    public static final int NOTIFICATION_CANCELLED_COST = 0;

    public Mono<Void> invalidateCosts(PaperCostToInvalidateInt req, String iun) {
        int recIndex = Integer.parseInt(req.getRecIndex());
        return Flux.fromIterable(req.getCostPhases())
                .flatMap(costPhase -> processCostPhase(req, iun, recIndex, costPhase))
                .then();
    }

    private Mono<CostComponentsInt> processCostPhase(PaperCostToInvalidateInt req, String iun, int recIndex, AnalogUpdateCostPhaseInt costPhase) {
        log.info("Start invalidateCosts for iun: {}, recIndex: {}, costPhase: {}", iun, recIndex, costPhase);
        return processPayment(req, iun, recIndex, costPhase);
    }

    private Mono<CostComponentsInt> processPayment(PaperCostToInvalidateInt req, String iun, int recIndex,
                                                   AnalogUpdateCostPhaseInt costPhase) {
        return costComponentService.getTotalCost(req.getVat(), iun, recIndex, req.getCreditorTaxId(), req.getNoticeCode())
                .flatMap(totalCost -> updateCostService.updateCostForInvalidated(recIndex, iun,
                                req.getCreditorTaxId(), req.getNoticeCode(),
                                totalCost, CostUpdateCostPhaseInt.valueOf(costPhase.getValue()), Instant.now(), Instant.now())
                .flatMap(updateCostResponse -> costComponentService.insertStepCost(CostUpdateCostPhaseInt.valueOf(costPhase.getValue()), iun, recIndex,
                                req.getCreditorTaxId(), req.getNoticeCode(), NOTIFICATION_CANCELLED_COST, req.getVat())
                        .doOnError(e -> log.error("An error occurred while inserting step cost for recIndex: {}. Error: {}",
                                recIndex, e.getMessage()))));
    }

}