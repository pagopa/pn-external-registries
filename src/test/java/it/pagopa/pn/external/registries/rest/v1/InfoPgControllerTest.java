package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {InfoPgController.class})
class InfoPgControllerTest {

    public static final String PN_PAGOPA_CX_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_UID = "x-pagopa-pn-uid";
    public static final String PN_PAGOPA_GROUPS = "x-pagopa-pn-cx-groups";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareGroupsService svc;

    @Test
    void getGroups() {
        // Given
        String url = "/ext-registry/pg/v1/groups";

        PgGroupDto dto1 = new PgGroupDto();
        dto1.setId("1");
        dto1.setName("n1");
        PgGroupDto dto2 = new PgGroupDto();
        dto2.setId("2");
        dto2.setName("n2");

        // When
        when(svc.getPgGroups(any(), any(), any(), any()))
                .thenReturn(Flux.fromIterable(List.of(dto1, dto2)));

        // Then
        webTestClient.get()
                .uri(url)
                .header(PN_PAGOPA_CX_ID, "userId")
                .header(PN_PAGOPA_UID, "uid")
                .header(PN_PAGOPA_GROUPS, "")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PgGroupDto.class).hasSize(2);
    }

    @Test
    void getPgUserGroup(){
        String url = "/ext-registry/pg/v1/user-groups";

        PgGroupDto dto1 = new PgGroupDto();
        dto1.setId("id1");
        dto1.setName("name1");
        PgGroupDto dto2 = new PgGroupDto();
        dto2.setId("id2");
        dto2.setName("name2");

        // When
        when(svc.getPgUserGroups("uid", "cxId", null)).thenReturn(Flux.fromIterable(List.of(dto1, dto2)));

        // Then
        webTestClient.get()
                .uri(url)
                .header(PN_PAGOPA_CX_ID, "cxId")
                .header(PN_PAGOPA_UID, "uid")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PgGroupDto.class)
                .hasSize(2);
    }
}