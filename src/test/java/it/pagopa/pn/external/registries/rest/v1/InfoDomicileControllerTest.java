package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.api.v1.mock.InfoDomicilieImpl;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {InfoDomicileController.class})
class InfoDomicileControllerTest {


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoDomicilieImpl svc;

    @Test
    void getOneAnalogDomicile() {

        // Given
        String url = "/ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/analog"
                .replace("{recipientType}", "PF")
                .replace("{opaqueId}", "f271e4bf-0d69-4ed6-a39f-4ef2f01f2fd1");

        AnalogDomicileDto dto = new AnalogDomicileDto();


        // When
        Mockito.when(svc.getOneAnalogDomicile(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(dto));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOneDigitalDomicile() {
        // Given
        String url = "/ext-registry-private/domiciles/v1/{recipientType}/{opaqueId}/digital"
                .replace("{recipientType}", "PF")
                .replace("{opaqueId}", "f271e4bf-0d69-4ed6-a39f-4ef2f01f2fd1");

        DigitalDomicileDto dto = new DigitalDomicileDto();


        // When
        Mockito.when(svc.getOneDigitalDomicile(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(dto));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk();
    }
}