package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {InfoInternalController.class})
class InfoInternalControllerTest {

    public static final String PN_PAGOPA_USER_ID = "x-pagopa-pn-cx-id";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareGroupsService svc;

    @Test
    void getAllPaGroups() {
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
        when(svc.getPaGroups(any(), any(), any(), any())).thenReturn(Flux.fromIterable(res));

        // Then
        webTestClient.get()
                .uri(url)
                .header(PN_PAGOPA_USER_ID, "internaluserid1234")
                .exchange()
                .expectStatus().isOk().expectBodyList(PaGroupDto.class).hasSize(2);
    }

    @Test
    void getAllPgGroups() {
        // Given
        String url = "/ext-registry-private/pg/v1/groups-all";

        PgGroupDto dto1 = new PgGroupDto();
        dto1.setId("id1");
        dto1.setName("name1");
        PgGroupDto dto2 = new PgGroupDto();
        dto2.setId("id2");
        dto2.setName("name2");

        // When
        when(svc.getPgGroups(any(), any(), any(), any())).thenReturn(Flux.fromIterable(List.of(dto1, dto2)));

        // Then
        webTestClient.get()
                .uri(url)
                .header(PN_PAGOPA_USER_ID, "internalUserId")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PgGroupDto.class)
                .hasSize(2);
    }
}
