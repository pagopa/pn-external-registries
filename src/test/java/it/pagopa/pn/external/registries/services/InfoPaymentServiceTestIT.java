package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.MockAWSObjectsTestConfig;
import it.pagopa.pn.external.registries.MockProducerTest;
import it.pagopa.pn.external.registries.generated.openapi.msclient.checkout.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentNoticeDto;
import it.pagopa.pn.external.registries.generated.openapi.server.payment.v1.dto.PaymentRequestDto;
import it.pagopa.pn.external.registries.middleware.msclient.CheckoutClient;
import it.pagopa.pn.external.registries.rest.v1.InfoPaymentController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@SpringBootTest
@Import({LocalStackTestConfig.class, MockAWSObjectsTestConfig.class})
@Slf4j
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class InfoPaymentServiceTestIT extends MockProducerTest {

    @Autowired
    private InfoPaymentService paymentService;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private CheckoutClient checkoutClient;

    @MockitoBean(name = "defaultApiClientCartCheckout")
    private DefaultApi defaultApiClientCartCheckout;

    @Autowired
    InfoPaymentController infoPaymentController;

    @Test
    void checkoutCartKo422() {

        Mockito.when(defaultApiClientCartCheckout.postCartsWithHttpInfo(Mockito.any()))
            .thenReturn(Mono.error(WebClientResponseException.create(422,"422",null,"test".getBytes(), Charset.defaultCharset())));

        final String url = "/ext-registry/pagopa/v1/checkout-cart";
        final String RETURN_URL = "https://portale.dev.pn.pagopa.it/notifiche/24556b11-c871-414e-92af-2583b481ffda/NMGY-QWAH-XGLK-202212-G-1/dettaglio";
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto()
            .paymentNotice(new PaymentNoticeDto()
                .noticeNumber("302012387654312384")
                .amount(1500)
                .fiscalCode("77777777777")
                .description("description")
                .companyName("companyName"))
            .returnUrl(RETURN_URL);


        webTestClient.post()
            .uri(url)
            .body(BodyInserters.fromPublisher(Mono.just(paymentRequestDto), PaymentRequestDto.class))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());

    }
}
