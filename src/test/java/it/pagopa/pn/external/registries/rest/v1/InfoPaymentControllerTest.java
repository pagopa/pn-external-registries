package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.exceptions.PnCheckoutBadRequestException;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.*;
import it.pagopa.pn.external.registries.services.InfoPaymentService;
import it.pagopa.pn.external.registries.services.SendPaymentNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST;
import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND;

@WebFluxTest(controllers = {InfoPaymentController.class})
class InfoPaymentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    InfoPaymentService service;

    @MockBean
    SendPaymentNotificationService sendPaymentNotificationService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getPaymentInfoSuccess() {
        //Given
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setStatus( PaymentStatusDto.SUCCEEDED );
        dto.setAmount( 20 );
        dto.setUrl( "https://api.uat.platform.pagopa.it/checkout/auth/payments/v2" );

        String url = "/ext-registry/pagopa/v1/paymentinfo/{paTaxId}/{noticeNumber}"
                .replace( "{paTaxId}", "77777777777" )
                .replace( "{noticeNumber}", "302000100000019421" );

        //When
        Mockito.when( service.getPaymentInfo( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( Mono.just( dto ) );

        //Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk();

    }

    @Test
    void checkoutCartOk() throws JsonProcessingException {
        final String url = "/ext-registry/pagopa/v1/checkout-cart";
        final String RETUNR_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto()
                .paymentNotice(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode("77777777777")
                        .description("description")
                        .companyName("companyName"))
                .returnUrl(RETUNR_URL);

        PaymentResponseDto expectedValue = new PaymentResponseDto().checkoutUrl(RETUNR_URL);

        Mockito.when( service.checkoutCart(paymentRequestDto)).thenReturn( Mono.just( expectedValue ) );

        webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromPublisher(Mono.just(paymentRequestDto), PaymentRequestDto.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(expectedValue));

        Mockito.verify(service, Mockito.times(1)).checkoutCart(paymentRequestDto);
    }

    @Test
    void checkoutCartKoBadRequestInput() {
        final String url = "/ext-registry/pagopa/v1/checkout-cart";
        final String RETUNR_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto()
                .paymentNotice(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode(null)
                        .description("description")
                        .companyName("companyName"))
                .returnUrl(RETUNR_URL);

        webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromPublisher(Mono.just(paymentRequestDto), PaymentRequestDto.class))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verify(service, Mockito.times(0)).checkoutCart(paymentRequestDto);
    }

    @Test
    void checkoutCartKoForException() {
        final String url = "/ext-registry/pagopa/v1/checkout-cart";
        final String RETUNR_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto()
                .paymentNotice(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode("77777777777")
                        .description("description")
                        .companyName("companyName"))
                .returnUrl(RETUNR_URL);

        Mockito.when( service.checkoutCart(paymentRequestDto)).thenThrow(new PnNotFoundException("", "", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_NOT_FOUND));

        webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromPublisher(Mono.just(paymentRequestDto), PaymentRequestDto.class))
                .exchange()
                .expectStatus().isNotFound();

        Mockito.verify(service, Mockito.times(1)).checkoutCart(paymentRequestDto);
    }

    @Test
    void checkoutCartKoForBadRequestOfCheckout() {
        final String url = "/ext-registry/pagopa/v1/checkout-cart";
        final String RETUNR_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto()
                .paymentNotice(new PaymentNoticeDto()
                        .noticeNumber("302012387654312384")
                        .amount(1500)
                        .fiscalCode("77777777777")
                        .description("description")
                        .companyName("companyName"))
                .returnUrl(RETUNR_URL);

        Mockito.when( service.checkoutCart(paymentRequestDto)).thenThrow(new PnCheckoutBadRequestException("", ERROR_CODE_EXTERNALREGISTRIES_CHECKOUT_BAD_REQUEST));

        webTestClient.post()
                .uri(url)
                .body(BodyInserters.fromPublisher(Mono.just(paymentRequestDto), PaymentRequestDto.class))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verify(service, Mockito.times(1)).checkoutCart(paymentRequestDto);
    }

}
