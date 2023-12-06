package it.pagopa.pn.external.registries.middleware.msclient.common;

import it.pagopa.pn.external.registries.exceptions.PnPaymentOngoingException;
import it.pagopa.pn.external.registries.generated.openapi.msclient.gpd.v1.dto.PaymentsModelResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient.PAYMENT_ONGOING_STATUS_CODE;

public class PaymentOnGoingInterceptor implements ExchangeFilterFunction {
    
    //Questo filtro verrà applicato a tutte le risposte
    @Override
    public @NotNull Mono<ClientResponse> filter(@NotNull ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .flatMap(response -> {
                    //in caso di 209 si vuole lanciare un exception che dovrà essere gestita dal chiamante
                    if (response.rawStatusCode() == PAYMENT_ONGOING_STATUS_CODE) {
                        Mono<PaymentsModelResponse> bodyMono = response.bodyToMono(PaymentsModelResponse.class);
                        Mono<HttpHeaders> headersMono = Mono.just(response.headers().asHttpHeaders());
                        
                        // Mono.zip utilizzato per combinare i risultati di più mono in un unico mono. 
                        return Mono.zip(bodyMono, headersMono)
                                .flatMap(tuple -> {
                                    PaymentsModelResponse errorBody = tuple.getT1();
                                    HttpHeaders headers = tuple.getT2();
                                    return Mono.error(new PnPaymentOngoingException(errorBody, headers, response.rawStatusCode()));
                                });
                    }
                    
                    //In tutti gli altri casi ritorna la risposta così come è
                    return Mono.just(response);
                });
    }
}