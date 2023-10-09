package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.ApiUtil;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.api.UpdateNotificationCostApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.UpdateNotificationCostRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.UpdateNotificationCostResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@AllArgsConstructor
public class UpdateNotificationCostController implements UpdateNotificationCostApi {
    @Override
    public Mono<ResponseEntity<UpdateNotificationCostResponseDto>> updateNotificationCost(Mono<UpdateNotificationCostRequestDto> updateNotificationCostRequestDto, final ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "{ \"iun\" : \"iun\", \"updateResults\" : [ { \"result\" : \"OK\", \"recIndex\" : 0, \"noticeCode\" : \"302000100000019421\", \"creditorTaxId\" : \"77777777777\" }, { \"result\" : \"OK\", \"recIndex\" : 0, \"noticeCode\" : \"302000100000019421\", \"creditorTaxId\" : \"77777777777\" } ] }";
                result = ApiUtil.getExampleResponse(exchange, mediaType, exampleString);
                break;
            }
        }
        return result.then(Mono.empty());
    }
}
