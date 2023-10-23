package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.PaymentForRecipientInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.UpdateNotificationCostApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentsInfoForRecipientDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.UpdateNotificationCostRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.UpdateNotificationCostResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.UpdateNotificationCostResultDto;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
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
public class UpdateNotificationCostController implements UpdateNotificationCostApi {
    private CostUpdateOrchestratorService service;
    
    @Override
    public Mono<ResponseEntity<UpdateNotificationCostResponseDto>> updateNotificationCost(Mono<UpdateNotificationCostRequestDto> updateNotificationCostRequestDto, final ServerWebExchange exchange) {
        return updateNotificationCostRequestDto.flatMap( res ->{
            final PaymentForRecipientInt[] paymentArray = getPaymentInfoForRecipient(res);
            return service.handleCostUpdateForIuvs(res.getNotificationStepCost(), 
                            res.getIun(),
                            paymentArray,
                            res.getEventTimestamp().toInstant(),
                            res.getEventStorageTimestamp().toInstant(),
                            CostUpdateCostPhaseInt.valueOf(res.getUpdateCostPhase().getValue())
                    )
                    .collectList();
            }
        ).map(responseList -> ResponseEntity.ok(getResponseDto(responseList)));
    }

    @NotNull
    private static PaymentForRecipientInt[] getPaymentInfoForRecipient(UpdateNotificationCostRequestDto res) {
        PaymentForRecipientInt[] paymentArray = new PaymentForRecipientInt[res.getPaymentsInfoForRecipients().size()];
        int index = 0;
        for(PaymentsInfoForRecipientDto paymentRequestDto : res.getPaymentsInfoForRecipients()) {
            PaymentForRecipientInt payment = new PaymentForRecipientInt();
            payment.setCreditorTaxId(paymentRequestDto.getCreditorTaxId());
            payment.setRecIndex(paymentRequestDto.getRecIndex());
            payment.setNoticeCode(paymentRequestDto.getNoticeCode());
            paymentArray[index] = payment;
            index = index + 1;
        }
        return paymentArray;
    }

    private UpdateNotificationCostResponseDto getResponseDto(List<UpdateCostResponseInt> responseList) {
        List<UpdateNotificationCostResultDto> resultList = new ArrayList<>();
        responseList.forEach(responseElement -> {
            UpdateNotificationCostResultDto resultDto = new UpdateNotificationCostResultDto();
            resultDto.setRecIndex(responseElement.getRecIndex());
            resultDto.setResult(UpdateNotificationCostResultDto.ResultEnum.valueOf(responseElement.getResult().getValue()));
            resultDto.setCreditorTaxId(responseElement.getCreditorTaxId());
            resultDto.setNoticeCode(responseElement.getNoticeCode());
            resultList.add(resultDto);
        });

        UpdateNotificationCostResponseDto dto = new UpdateNotificationCostResponseDto();
        dto.setUpdateResults(resultList);
        return dto;
    }
}
