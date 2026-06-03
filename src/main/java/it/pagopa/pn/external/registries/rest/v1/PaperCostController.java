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
import java.util.Objects;

@RestController
@Slf4j
@AllArgsConstructor
public class PaperCostController implements PaperCostApi {
    private PaperCostService service;
    
    @Override
    public Mono<ResponseEntity<Void>> invalidatePaperCost(String iun, Mono<PaperCostToInvalidateDto> paperCostToInvalidateDto, final ServerWebExchange exchange) {
        return paperCostToInvalidateDto
                .doOnNext(dto -> log.debug("Received invalidatePaperCost request for iun={} with costPhases={}, vat={}, paymentsInfo={} ",
                        iun, dto.getCostPhases(), dto.getVat(), dto.getPaymentsInfo()))
                .flatMap(this::validateData)
                .flatMap(this::getInternalPaperCostToInvalidate)
                .doOnNext(internalReq -> log.debug("Mapped invalidatePaperCost request for iun={} to internal DTO with costPhases={}, vat={}, paymentInfoListSize={}, paymentInfoList={}",
                        iun,
                        internalReq.getCostPhases(),
                        internalReq.getVat(),
                        internalReq.getPaymentInfoList() == null ? null : internalReq.getPaymentInfoList().size(),
                        internalReq.getPaymentInfoList()))
                .flatMap(internalReq -> service.invalidateCosts(internalReq, iun))
                .thenReturn(ResponseEntity.noContent().build());
    }

    private Mono<PaperCostToInvalidateDto> validateData(PaperCostToInvalidateDto paperCostToInvalidateDto) {
        if (Objects.isNull(paperCostToInvalidateDto.getPaymentsInfo())) {
            return Mono.error(new IllegalArgumentException("Payments info list cannot be null"));
        }

        return Mono.just(paperCostToInvalidateDto);
    }

    private Mono<PaperCostToInvalidateInt> getInternalPaperCostToInvalidate(PaperCostToInvalidateDto dto) {
        return Mono.just(PaperCostToInvalidateInt
                .builder()
                .vat(dto.getVat())
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
                .build()).toList();
    }

    private List<CostUpdateCostPhaseInt> getPaperCostToInvalidateInt(List<AnalogUpdateCostPhaseDto> dtoList) {
        return dtoList.stream().map(entity -> CostUpdateCostPhaseInt.valueOf(entity.getValue())).toList();
    }
}
