package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class InfoPaymentService {
    public static final String JSON_PROCESSING_ERROR_MSG = "Unable to map response from checkout to paymentInfoDto paymentId={}";
    private final CheckoutClient checkoutClient;
    private final PnExternalRegistriesConfig config;
    private final SendPaymentNotificationService sendPaymentNotificationService;

    public InfoPaymentService(CheckoutClient checkoutClient, PnExternalRegistriesConfig config,
                              SendPaymentNotificationService sendPaymentNotificationService) {
        this.checkoutClient = checkoutClient;
        this.config = config;
        this.sendPaymentNotificationService = sendPaymentNotificationService;
    }

    public Mono<PaymentInfoDto> getPaymentInfo(String paTaxId, String noticeNumber) {
        String paymentId = paTaxId + noticeNumber;
        
        log.info( "get payment info paymentId={}", paymentId );
        return checkoutClient.getPaymentInfo(paymentId)
                .map(paymentInfoResponse0 -> new PaymentInfoDto()
                    .status( PaymentStatusDto.REQUIRED )
                    .amount( paymentInfoResponse0.getImportoSingoloVersamento() )
                    .url( config.getCheckoutSiteUrl() )
                )
                .onErrorResume( WebClientResponseException.class, ex -> {
                    HttpStatus httpStatus = ex.getStatusCode();
                    log.info( "Get checkout payment info status code={} paymentId={}", httpStatus, paymentId );
                    switch (httpStatus) {
                        case NOT_FOUND: { return fromCheckoutNotFoundToPn( paymentId, ex.getResponseBodyAsString() ); }
                        case CONFLICT: { return fromCheckoutConflictToPn( paTaxId, noticeNumber, ex.getResponseBodyAsString() ); }
                        case BAD_GATEWAY: { return fromCheckoutBadGatewayToPn( paymentId, ex.getResponseBodyAsString() ); }
                        case SERVICE_UNAVAILABLE: { return fromCheckoutServiceUnavToPn( paymentId, ex.getResponseBodyAsString() ); }
                        case GATEWAY_TIMEOUT: { return fromCheckoutGWTimeoutToPn( paymentId, ex.getResponseBodyAsString() ); }
                        default: throw new UnsupportedOperationException( String.format("Unable to manage status response from checkout for paymentId=%s", paymentId) );
                    }
        });
    }

    private Mono<PaymentInfoDto> fromCheckoutGWTimeoutToPn(String paymentId, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PartyTimeoutFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PartyTimeoutFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getCategory() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error(JSON_PROCESSING_ERROR_MSG, paymentId);
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paymentId, e);
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutServiceUnavToPn(String paymentId, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PartyConfigurationFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PartyConfigurationFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getCategory() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( JSON_PROCESSING_ERROR_MSG, paymentId );
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paymentId, e);
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutBadGatewayToPn(String paymentId, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        GatewayFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, GatewayFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getCategory() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( JSON_PROCESSING_ERROR_MSG, paymentId );
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paymentId, e);
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutConflictToPn(String paTaxId, String noticeNumber, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentStatusFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PaymentStatusFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getCategory() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
                return sendPaymentNotificationService.sendPaymentNotification( paTaxId, noticeNumber ).thenReturn(paymentInfoDto);
            } else {
                log.error( JSON_PROCESSING_ERROR_MSG, paTaxId+noticeNumber );
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paTaxId+noticeNumber, e);
        }
        return Mono.just( paymentInfoDto );
    }

    private Mono<PaymentInfoDto> fromCheckoutNotFoundToPn(String paymentId, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        ValidationFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, ValidationFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue( result.getCategory() );
                paymentInfoDto.setDetail( detailDto );
                paymentInfoDto.setDetailV2( result.getDetailV2() );
                paymentInfoDto.setStatus( getPaymentStatus( detailDto ) );
            } else {
                log.error( JSON_PROCESSING_ERROR_MSG, paymentId );
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paymentId, e);
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
