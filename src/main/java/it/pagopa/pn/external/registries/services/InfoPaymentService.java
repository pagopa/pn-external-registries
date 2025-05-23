package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.*;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.dto.PaymentNoticeDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.*;

@Service
@CustomLog
public class InfoPaymentService {
    public static final String JSON_PROCESSING_ERROR_MSG = "Unable to map response from checkout to paymentInfoDto paTaxId=%s noticeCode=%s";
    private final CheckoutClient checkoutClient;
    private final PnExternalRegistriesConfig config;

    public InfoPaymentService(CheckoutClient checkoutClient, PnExternalRegistriesConfig config) {
        this.checkoutClient = checkoutClient;
        this.config = config;
    }

    public Mono<List<PaymentInfoV21Dto>> getPaymentInfo(Flux<PaymentInfoRequestDto> paymentInfoRequestDtoFlux) {
        return paymentInfoRequestDtoFlux
                .flatMap(this::callCheckoutPaymentInfo)
                .collectList();
    }

    private Mono<PaymentInfoV21Dto> callCheckoutPaymentInfo(PaymentInfoRequestDto request) {
        String paTaxId = request.getCreditorTaxId();
        String noticeNumber = request.getNoticeCode();
        String paymentId = paTaxId + noticeNumber;

        log.info( "get payment info paymentId={}", paymentId );
        return checkoutClient.getPaymentInfo(paymentId)
                .map(paymentRequestsGetResponseDto -> apiResponseToPaymentInfoV21InnerDtoV2(paymentRequestsGetResponseDto, request))
                .onErrorResume( WebClientResponseException.class, ex -> {
                    HttpStatus httpStatus = ex.getStatusCode();
                    log.info( "Get checkout payment info status code={} paymentId={}", httpStatus, paymentId );
                    return fromCheckoutToPn( paTaxId, noticeNumber, httpStatus, ex.getResponseBodyAsString() );
                });
    }

    private PaymentInfoV21Dto apiResponseToPaymentInfoV21InnerDtoV2(PaymentRequestsGetResponseDto paymentInfoResponse, PaymentInfoRequestDto request) {
        return new PaymentInfoV21Dto()
                .status(PaymentStatusDto.REQUIRED)
                .amount(paymentInfoResponse.getAmount())
                .url(config.getCheckoutSiteUrl())
                .creditorTaxId(request.getCreditorTaxId())
                .noticeCode(request.getNoticeCode())
                .causaleVersamento(paymentInfoResponse.getDescription() != null ? paymentInfoResponse.getDescription() : null)
                .dueDate(paymentInfoResponse.getDueDate() != null ? paymentInfoResponse.getDueDate() : null);
    }

    private Mono<PaymentInfoV21Dto> fromCheckoutToPn(String paTaxId, String noticeNumber, HttpStatus status, String checkoutResult) {
        log.info( checkoutResult );
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentStatusConflictDto result;

        PaymentInfoV21Dto paymentInfoDto = new PaymentInfoV21Dto();
        paymentInfoDto.setCreditorTaxId(paTaxId);
        paymentInfoDto.setNoticeCode(noticeNumber);

        try {
            checkoutStatusManagement(status);

            result = objectMapper.readValue( checkoutResult, PaymentStatusConflictDto.class );
            if (result != null) {
                DetailDto detailDto = DetailDto.fromValue(result.getFaultCodeCategory().getValue());
                paymentInfoDto.setDetail(detailDto);
                paymentInfoDto.setDetailV2(result.getFaultCodeDetail().getValue());
                paymentInfoDto.setStatus(getPaymentStatus(detailDto));
            }
        } catch (JsonProcessingException e) {
            log.error(String.format(JSON_PROCESSING_ERROR_MSG, paTaxId, noticeNumber), e);
            paymentInfoDto.setDetail(DetailDto.GENERIC_ERROR);
            paymentInfoDto.setStatus(PaymentStatusDto.FAILURE);
            paymentInfoDto.setDetailV2(String.format(JSON_PROCESSING_ERROR_MSG, paTaxId, noticeNumber));
        } catch (PnCheckoutBadRequestException | PnCheckoutServerErrorException | PnCheckoutNotFoundException | PnCheckoutUnauthorizedException  e) {
            if (e instanceof PnCheckoutServerErrorException){
                log.error(
                    "Get checkout payment error status code={}, paTaxId={} noticenumber={}",
                    status, paTaxId, noticeNumber, e
                );
            }
            else if (e instanceof PnCheckoutUnauthorizedException){
                log.fatal("Authorization failure while retrieving payment info. " +
                                "paTaxId={}, noticeNumber={}",
                        paTaxId, noticeNumber, e);
            }
            else {
                log.warn(
                    "Get checkout payment error status code={}, paTaxId={} noticenumber={}",
                    status, paTaxId, noticeNumber, e
                );
            }
           buildPaymentInfoError(e, paymentInfoDto, status);
        }
        return Mono.just( paymentInfoDto );
    }

    private void buildPaymentInfoError(Exception e, PaymentInfoV21Dto paymentInfoDto, HttpStatus status) {
        paymentInfoDto.setDetail(DetailDto.GENERIC_ERROR);
        paymentInfoDto.setStatus(PaymentStatusDto.FAILURE);
        paymentInfoDto.setDetailV2(e.getMessage());

        if (e instanceof PnCheckoutUnauthorizedException) {
            paymentInfoDto.setErrorCode(e.getMessage());
        } else {
            paymentInfoDto.setErrorCode(status.toString());
        }
    }

    private PaymentStatusDto getPaymentStatus(DetailDto detail) {
        return switch (detail) {
            case PAYMENT_ONGOING -> PaymentStatusDto.IN_PROGRESS;
            case PAYMENT_DUPLICATED -> PaymentStatusDto.SUCCEEDED;
            default -> PaymentStatusDto.FAILURE;
        };
    }

    private void checkoutStatusManagement( HttpStatus status) {
        if (HttpStatus.BAD_REQUEST.equals(status)) {
            throw new PnCheckoutBadRequestException(
                    "Formally invalid input",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST);
        }
        if (HttpStatus.NOT_FOUND.equals(status)) {
            throw new PnCheckoutNotFoundException(
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
        if(HttpStatus.UNAUTHORIZED.equals(status)) {
            throw new PnCheckoutUnauthorizedException(
                    "Downstream checkout not available",
                    ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_UNAUTHORIZED);
        }
    }

    public Mono<PaymentResponseDto> checkoutCart(PaymentRequestDto paymentRequestDto) {

        log.info( "checkoutCart info paymentId={}", paymentRequestDto );
        CartRequestDto cartRequestDto = toCartRequestDto(paymentRequestDto);

        return checkoutClient.checkoutCart(cartRequestDto)
                .doOnNext(voidResponseEntity -> log.info("Response Status from checkout for noticeNumber {}: {}",
                        paymentRequestDto.getPaymentNotice().getNoticeNumber(), voidResponseEntity.getStatusCode()))
                .map( this::manageCheckoutResponse)
            .onErrorResume(error -> {
                if (error instanceof WebClientResponseException.UnprocessableEntity){
                    throw new PnUnprocessableEntityException("Checkout bad request", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST);
                } else  if (error instanceof  WebClientResponseException.BadRequest){
                    throw new PnCheckoutBadRequestException("Checkout bad request", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST);
                } else {
                    throw new PnNotFoundException("Checkout error: " + error.getMessage(), "", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND);
                }

            })
            .doOnError(throwable -> log.error(String.format("Error in checkoutCart with noticeNumber %s",
                        paymentRequestDto.getPaymentNotice().getNoticeNumber()), throwable));
    }
    private PaymentResponseDto manageCheckoutResponse(ResponseEntity<Void> httpResponse) {
        if(httpResponse.getStatusCode().value() == 302) {
            return buildPaymentResponseDto(httpResponse.getHeaders().getLocation());
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

}
