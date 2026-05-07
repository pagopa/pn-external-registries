package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.PaperCostApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.UpdateNotificationCostApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
import it.pagopa.pn.external.registries.services.PaperCostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
                .creditorTaxId(dto.getCreditorTaxId())
                .noticeCode(dto.getNoticeCode())
                .recIndex(dto.getRecIndex())
                        .costPhases(getPaperCostToInvalidateInt(dto.getCostPhases()))
                .build());
    }

    private List<AnalogUpdateCostPhaseInt> getPaperCostToInvalidateInt(List<AnalogUpdateCostPhaseDto> dtoList) {
        return dtoList.stream().map(entity -> AnalogUpdateCostPhaseInt.valueOf(entity.getValue())).toList();
    }
}
