package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.dto.CostComponentsInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaperCostToInvalidateInt;
import it.pagopa.pn.external.registries.dto.PaymentInfoInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsWithDebtorInfoModelResponse;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class PaperCostService {

    private final CostComponentService costComponentService;
    private final UpdateCostService updateCostService;
    private final GpdClient gpdClient;
    public static final int INVALIDATED_COST = 0;

    public Mono<Void> invalidateCosts(PaperCostToInvalidateInt req, String iun) {
        return checkPaymentsStatus(req.getPaymentInfoList())
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
                .filter(updateCostResponse -> paymentInfo.isApplyCost())
                .flatMap(updateCostResponse -> costComponentService.insertStepCost(costPhase, iun, recIndex,
                                paymentInfo.getCreditorTaxId(), paymentInfo.getNoticeCode(), INVALIDATED_COST, vat)
                .doOnError(e -> log.error("An error occurred while inserting step cost for recIndex: {}. Error: {}",
                        recIndex, e.getMessage()))));
    }

    private Mono<List<PaymentInfoInt>> checkPaymentsStatus(List<PaymentInfoInt> paymentInfoList) {
        if (paymentInfoList == null || paymentInfoList.isEmpty()) {
            return Mono.error(new IllegalStateException("The cost cannot be invalidated because the payment information is not available"));
        }

        return Flux.fromIterable(paymentInfoList)
                .concatMap(paymentInfo -> gpdClient.getOrganizationPaymentOptionByNAV(paymentInfo.getCreditorTaxId(), paymentInfo.getNoticeCode())
                        .flatMap(this::checkStatusEnum)
                        .thenReturn(paymentInfo))
                .collectList();
    }


    private Mono<PaymentsWithDebtorInfoModelResponse> checkStatusEnum(PaymentsWithDebtorInfoModelResponse response) {
        if(Objects.isNull(response.getStatus())) return Mono.error(new IllegalStateException("The cost cannot be invalidated because the payment status information is not available"));
        return switch (response.getStatus()) {
            case PO_PAID, PO_PARTIALLY_REPORTED, PO_REPORTED ->
                    Mono.error(new PnInternalException("Pagato.", 422, ""));
            case PO_UNPAID -> Mono.just(response);
        };
    }
}