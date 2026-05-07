package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaperCostToInvalidateInt;
import it.pagopa.pn.external.registries.dto.PaymentInfoInt;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.PaperCostApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AnalogUpdateCostPhaseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaperCostToInvalidateDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaymentsInfoDto;
import it.pagopa.pn.external.registries.services.PaperCostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
public class PaperCostController implements PaperCostApi {
    private PaperCostService service;
    
    @Override
    public Mono<ResponseEntity<Void>> invalidatePaperCost(String iun, Mono<PaperCostToInvalidateDto> paperCostToInvalidateDto, final ServerWebExchange exchange) {
        return paperCostToInvalidateDto
                .flatMap(this::getInternalPaperCostToInvalidate)
                .flatMap(internalReq -> service.invalidateCosts(internalReq, iun))
                .map(responseList -> ResponseEntity.ok().build());
    }

    private Mono<PaperCostToInvalidateInt> getInternalPaperCostToInvalidate(PaperCostToInvalidateDto dto) {
        return Mono.just(PaperCostToInvalidateInt
                .builder()
                .vat(dto.getVat())
                .recIndex(dto.getRecIndex())
                .costPhases(getPaperCostToInvalidateInt(dto.getCostPhases()))
                .paymentInfoList(getPaymentInfoInt(dto.getPaymentsInfo()))
                .build());
    }

    private List<PaymentInfoInt> getPaymentInfoInt(List<PaymentsInfoDto> paymentsInfo) {
        return paymentsInfo.stream().map(paymentInfoDto -> PaymentInfoInt
                .builder()
                .creditorTaxId(paymentInfoDto.getCreditorTaxId())
                .noticeCode(paymentInfoDto.getNoticeCode())
                .recIndex(paymentInfoDto.getRecIndex())
                .applyCost(paymentInfoDto.getApplyCost())
                .build()).toList();
    }

    private List<CostUpdateCostPhaseInt> getPaperCostToInvalidateInt(List<AnalogUpdateCostPhaseDto> dtoList) {
        return dtoList.stream().map(entity -> CostUpdateCostPhaseInt.valueOf(entity.getValue())).toList();
    }
}
