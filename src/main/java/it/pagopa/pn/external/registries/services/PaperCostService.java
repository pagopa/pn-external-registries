package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaperCostToInvalidateInt;
import it.pagopa.pn.external.registries.dto.PaymentInfoInt;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class PaperCostService {

    private final CostComponentService costComponentService;
    private final UpdateCostService updateCostService;
    public static final int INVALIDATED_COST = 0;

    public Mono<Void> invalidateCosts(PaperCostToInvalidateInt req, String iun) {
        return validatePaymentInfoList(req.getPaymentInfoList())
                .flatMapMany(paymentInfoList -> Flux.fromIterable(paymentInfoList)
                        .concatMap(paymentInfo -> Flux.fromIterable(req.getCostPhases())
                                .concatMap(costPhase -> processCostPhase(iun, req.getVat(), paymentInfo.getRecIndex(), costPhase, paymentInfo))))
                .then();
    }

    private Mono<CostComponentsInt> processCostPhase(String iun, Integer vat, int recIndex, CostUpdateCostPhaseInt costPhase,
                                                     PaymentInfoInt paymentInfo) {
        log.info("Start invalidateCosts for iun: {}, recIndex: {}, costPhase: {}", iun, recIndex, costPhase);
        Instant now = Instant.now();
        return costComponentService.getTotalCost(vat, iun, recIndex, paymentInfo.getCreditorTaxId(), paymentInfo.getNoticeCode())
                .flatMap(totalCost -> updateCostService.updateCostForInvalidated(recIndex, iun,
                                paymentInfo.getCreditorTaxId(), paymentInfo.getNoticeCode(),
                                totalCost, costPhase, now, now)
                .flatMap(updateCostResponse -> costComponentService.insertStepCost(costPhase, iun, recIndex,
                                paymentInfo.getCreditorTaxId(), paymentInfo.getNoticeCode(), INVALIDATED_COST, vat)
                .doOnError(e -> log.error("An error occurred while inserting step cost for recIndex: {}. Error: {}",
                        recIndex, e.getMessage()))));
    }

    private Mono<List<PaymentInfoInt>> validatePaymentInfoList(List<PaymentInfoInt> paymentInfoList) {
        if (paymentInfoList == null || paymentInfoList.isEmpty()) {
            return Mono.error(new IllegalStateException("The cost cannot be invalidated because the payment information is not available"));
        }

        return Mono.just(paymentInfoList);
    }
}