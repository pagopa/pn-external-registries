package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.*;
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

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.*;

@Service
@Slf4j
public class InfoPaymentService {
    public static final String JSON_PROCESSING_ERROR_MSG = "Unable to map response from checkout to paymentInfoDto paTaxId={} noticeCode={}";
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
                    return fromCheckoutToPn( paTaxId, noticeNumber, httpStatus, ex.getResponseBodyAsString() );
        });
    }

    private Mono<PaymentInfoDto> checkoutStatusManagement(String paTaxId, String noticeNumber, HttpStatus status, PaymentInfoDto paymentInfoDto) {
        if (HttpStatus.CONFLICT.equals(status) && DetailDto.PAYMENT_DUPLICATED.equals(paymentInfoDto.getDetail()) ) {
            return sendPaymentNotificationService.sendPaymentNotification(paTaxId, noticeNumber).thenReturn(paymentInfoDto);
        }
        if (HttpStatus.BAD_REQUEST.equals(status)) {
            throw new PnCheckoutBadRequestException(
                    "Formally invalid input",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST);
        }
        if (HttpStatus.NOT_FOUND.equals(status)) {
            throw new PnCheckoutBadRequestException(
                    "Node cannot find the services needed to process this request in its configuration." +
                            "This error is most likely to occur when submitting a non-existing RPT id",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND);
        }
        if (HttpStatus.BAD_GATEWAY.equals(status)) {
            throw new PnCheckoutServerErrorException(
                    "PagoPA services are not available or request is rejected by PagoPa",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_GATEWAY);
        }
        if (HttpStatus.SERVICE_UNAVAILABLE.equals(status)) {
            throw new PnCheckoutServerErrorException(
                    "EC services are not available",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_SERVICE_UNAVAILABLE);
        }
        if (HttpStatus.GATEWAY_TIMEOUT.equals(status)) {
            throw new PnCheckoutServerErrorException(
                    "Timeout from PagoPA services",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_GATEWAY_TIMEOUT);
        }
        return Mono.just(paymentInfoDto);
    }

    private Mono<PaymentInfoDto> fromCheckoutToPn(String paTaxId, String noticeNumber, HttpStatus status, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentStatusFaultPaymentProblemJsonDto result;
        PaymentInfoDto paymentInfoDto = new PaymentInfoDto();
        try {
            result = objectMapper.readValue( checkoutResult, PaymentStatusFaultPaymentProblemJsonDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue(result.getCategory());
                paymentInfoDto.setDetail(detailDto);
                paymentInfoDto.setDetailV2(result.getDetailV2());
                paymentInfoDto.setStatus(getPaymentStatus(detailDto));
                return checkoutStatusManagement(paTaxId, noticeNumber, status, paymentInfoDto);
            }
        } catch (JsonProcessingException e) {
            log.error(JSON_PROCESSING_ERROR_MSG, paTaxId, noticeNumber, e);
            return Mono.error( e );
        } catch (PnCheckoutBadRequestException | PnCheckoutServerErrorException e) {
            log.error(
                    "Get checkout payment error status code={}, paTaxId={} noticenumber={}",
                    status, paTaxId, noticeNumber, e
            );
            return Mono.error( e );
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
