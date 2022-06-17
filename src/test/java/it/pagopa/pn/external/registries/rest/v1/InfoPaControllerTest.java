package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@WebFluxTest(controllers = {InfoPaController.class})
class InfoPaControllerTest {


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareService svc;

    @Test
    void getOnePa() {

        // Given
        String url = "/ext-registry-private/pa/v1/activated-on-pn/{id}"
                .replace("{id}", "123456789");

        PaInfoDto dto = new PaInfoDto();
        dto.setId("123456789");
        dto.setName("pubblica amministrazione XXX");
        dto.setTaxId("123456789");

        // When
        Mockito.when(svc.getOnePa(Mockito.anyString()))
                .thenReturn(Mono.just(dto));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listOnboardedPa() {

        // Given
        String url = "/ext-registry/pa/v1/activated-on-pn";

        List<PaSummaryDto> res = new ArrayList<>();
        PaSummaryDto dto = new PaSummaryDto();
        dto.setId("123456789");
        dto.setName("pubblica amministrazione XXX");
        res.add(dto);
        dto = new PaSummaryDto();
        dto.setId("987654321");
        dto.setName("pubblica amministrazione YYY");
        res.add(dto);

        // When
        Mockito.when(svc.listOnboardedPaByName(Mockito.any()))
                .thenReturn(Flux.fromIterable(res));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk().expectBodyList(PaSummaryDto.class).hasSize(2);
    }

    @Test
    void listOnboardedPaFilter() {

        // Given
        String url = "/ext-registry/pa/v1/activated-on-pn?id={}"
                .replace("{id}", "123456789");

        List<PaSummaryDto> res = new ArrayList<>();
        PaSummaryDto dto = new PaSummaryDto();
        dto.setId("123456789");
        dto.setName("pubblica amministrazione XXX");
        res.add(dto);


        // When
        Mockito.when(svc.listOnboardedPaByIds(Mockito.anyList()))
                .thenReturn(Flux.fromIterable(res));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk().expectBodyList(PaSummaryDto.class).hasSize(1);
    }
}