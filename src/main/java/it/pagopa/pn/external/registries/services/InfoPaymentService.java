package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.DetailDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InfoPaymentService {
    public static final String MSG = "Unable to map response from checkout to paymentInfoDto";
    private final CheckoutClient checkoutClient;

    public InfoPaymentService(CheckoutClient checkoutClient) {
        this.checkoutClient = checkoutClient;
    }

    public Mono<PaymentInfoDto> getPaymentInfo(String paymentId) {
        try {
            PaymentRequestsGetResponseDto result = checkoutClient.getPaymentInfo( paymentId ).block();
            PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
            if (result != null) {
                paymentInfoDto.setStatus( PaymentStatusDto.REQUIRED );
                paymentInfoDto.setAmount( result.getImportoSingoloVersamento() );
            }
            return Mono.just( paymentInfoDto );
        } catch ( WebClientResponseException ex) {
            HttpStatus httpStatus = ex.getStatusCode();
            switch (httpStatus) {
                case NOT_FOUND: { return fromCheckoutNotFoundToPn( ex.getResponseBodyAsString() ); }
                case CONFLICT: { return fromCheckoutConflictToPn( ex.getResponseBodyAsString() ); }
                case BAD_GATEWAY: { return fromCheckoutBadGatewayToPn( ex.getResponseBodyAsString() ); }
                case SERVICE_UNAVAILABLE: { return fromCheckoutServiceUnavToPn( ex.getResponseBodyAsString() ); }
                case GATEWAY_TIMEOUT: { return fromCheckoutGWTimeoutToPn( ex.getResponseBodyAsString() ); }
                default: throw new UnsupportedOperationException( String.format("Unable to manage status response from checkout for paymentId=%s", paymentId) );
            }
        }
    }

    private Mono<PaymentInfoDto> fromCheckoutGWTimeoutToPn(String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PartyTimeoutFaultPaymentProblemJsonDto result = null;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PartyTimeoutFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getDetail() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error(MSG);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutServiceUnavToPn(String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PartyConfigurationFaultPaymentProblemJsonDto result = null;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PartyConfigurationFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getDetail() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( MSG );
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutBadGatewayToPn(String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayFaultPaymentProblemJsonDto result = null;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, GatewayFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getDetail() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( MSG );
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutConflictToPn(String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentStatusFaultPaymentProblemJsonDto result = null;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PaymentStatusFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getDetail() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( MSG );
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutNotFoundToPn(String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        ValidationFaultPaymentProblemJsonDto result = null;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, ValidationFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getDetail() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( MSG );
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.just( paymentInfoDto );
    }

    private PaymentStatusDto getPaymentStatus(DetailDto detail) {
        switch (detail) {
            case PAYMENT_ONGOING: return PaymentStatusDto.IN_PROGRESS;
            case PAYMENT_DUPLICATED: return PaymentStatusDto.SUCCEEDED;
            default: return PaymentStatusDto.FAILURE;
        }
    }
}