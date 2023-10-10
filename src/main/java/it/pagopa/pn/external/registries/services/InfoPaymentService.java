package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.*;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.PaymentNoticeDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.*;

@Service
@Slf4j
public class InfoPaymentService {
    public static final String JSON_PROCESSING_ERROR_MSG = "Unable to map response from checkout to paymentInfoDto paTaxId={} noticeCode={}";
    private final CheckoutClient checkoutClient;
    private final PnExternalRegistriesConfig config;

    public InfoPaymentService(CheckoutClient checkoutClient, PnExternalRegistriesConfig config) {
        this.checkoutClient = checkoutClient;
        this.config = config;
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

    private Mono<PaymentInfoDto> checkoutStatusManagement( HttpStatus status, PaymentInfoDto paymentInfoDto) {
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
                return checkoutStatusManagement(status, paymentInfoDto);
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
        return switch (detail) {
            case PAYMENT_ONGOING -> PaymentStatusDto.IN_PROGRESS;
            case PAYMENT_DUPLICATED -> PaymentStatusDto.SUCCEEDED;
            default -> PaymentStatusDto.FAILURE;
        };
    }

    public Mono<PaymentResponseDto> checkoutCart(PaymentRequestDto paymentRequestDto) {

        log.info( "checkoutCart info paymentId={}", paymentRequestDto );
        CartRequestDto cartRequestDto = toCartRequestDto(paymentRequestDto);

        return checkoutClient.checkoutCart(cartRequestDto)
                .doOnNext(voidResponseEntity -> log.info("Response Status from checkout for noticeNumber {}: {}",
                        paymentRequestDto.getPaymentNotice().getNoticeNumber(), voidResponseEntity.getStatusCode()))
                .map( this::manageCheckoutResponse)
                .doOnError(throwable -> log.error(String.format("Error in checkoutCart with noticeNumber %s",
                        paymentRequestDto.getPaymentNotice().getNoticeNumber()), throwable));
    }

    private PaymentResponseDto manageCheckoutResponse(ResponseEntity<Void> httpResponse) {
        if(httpResponse.getStatusCode().value() == 302) {
            return buildPaymentResponseDto(httpResponse.getHeaders().getLocation());
        }

        if(httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new PnCheckoutBadRequestException("Checkout bad request", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST);
        }
        throw new PnNotFoundException("Checkout postPayment status response " + httpResponse.getStatusCode(), "", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND);
    }

    protected CartRequestDto toCartRequestDto(PaymentRequestDto paymentRequestDto) {
        PaymentNoticeDto paymentNoticeDto = new PaymentNoticeDto()
                .noticeNumber(paymentRequestDto.getPaymentNotice().getNoticeNumber())
                .companyName(paymentRequestDto.getPaymentNotice().getCompanyName())
                .fiscalCode(paymentRequestDto.getPaymentNotice().getFiscalCode())
                .amount(paymentRequestDto.getPaymentNotice().getAmount())
                .description(paymentRequestDto.getPaymentNotice().getDescription());

        return new CartRequestDto()
                .paymentNotices(List.of(paymentNoticeDto))
                .returnUrls(new CartRequestReturnUrlsDto()
                        .returnOkUrl(URI.create(paymentRequestDto.getReturnUrl()))
                        .returnCancelUrl(URI.create(paymentRequestDto.getReturnUrl()))
                        .returnErrorUrl(URI.create(paymentRequestDto.getReturnUrl()))
                );
    }

    private PaymentResponseDto buildPaymentResponseDto(URI uri) {
        assert (uri != null );
        return new PaymentResponseDto().checkoutUrl(uri.toString());
    }

    @NotNull
    private String formatInstantToString(Instant instantToFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
        return formatter.format(instantToFormat);
    }

}
