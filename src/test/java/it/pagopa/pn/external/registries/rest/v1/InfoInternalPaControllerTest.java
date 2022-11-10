package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareServiceMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@WebFluxTest(controllers = {InfoInternalPaController.class})
class InfoInternalPaControllerTest {

    public static final String PN_PAGOPA_USER_ID = "x-pagopa-pn-cx-id";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareServiceMock svc;

    @Test
    void getAllGroups() {

        // Given
        String url = "/ext-registry-private/pa/v1/groups-all";

        List<PaGroupDto> res = new ArrayList<>();
        PaGroupDto dto = new PaGroupDto();
        dto.setId("123456789");
        dto.setName("amministrazione");
        res.add(dto);
        dto = new PaGroupDto();
        dto.setId("987654321");
        dto.setName("dirigenza");
        res.add(dto);

        // When
        Mockito.when(svc.getGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Flux.fromIterable(res));


        // Then
        webTestClient.get()
                .uri(url)
                .header( PN_PAGOPA_USER_ID, "internaluserid1234")
                .exchange()
                .expectStatus().isOk().expectBodyList(PaGroupDto.class).hasSize(2);
    }
}
