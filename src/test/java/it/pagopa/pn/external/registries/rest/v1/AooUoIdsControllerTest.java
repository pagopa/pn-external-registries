package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(AooUoIdsController.class)
class AooUoIdsControllerTest {
    private static final String URL = "/ext-registry-private/pa/v1/actions/filter-out-root-pa-ids?id={id}"
        .replace("{id}", "A123456789");

    private static final String URL_V2 = "/ext-registry-private/pa/v2/actions/filter-out-root-pa-ids?id={id}"
            .replace("{id}", "A123456789");


    @MockitoBean
    InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void getFilteredIdsOK()  {

        List<String> values = List.of("A123456789");
        Mockito.when(infoSelfcareInstitutionsService.filterOutRootIds(values))
            .thenReturn(Flux.fromIterable(values));

        // Then
        webTestClient.get()
            .uri(URL)
            .exchange()
            .expectStatus().isOk().expectBodyList(String.class).hasSize(1);
    }

    @Test
    void getFilteredIdsV2OK()  {

        List<String> values = List.of("A123456789");
        Mockito.when(infoSelfcareInstitutionsService.filterOutRootIds(values))
                .thenReturn(Flux.fromIterable(values));

        // Then
        webTestClient.get()
                .uri(URL_V2)
                .exchange()
                .expectStatus().isOk().expectBodyList(String.class).hasSize(1);
    }
}
