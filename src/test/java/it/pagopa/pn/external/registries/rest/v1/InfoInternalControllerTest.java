package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDetailDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {InfoInternalController.class})
class InfoInternalControllerTest {

    public static final String X_PAGOPA_PN_CX_ID = "x-pagopa-pn-cx-id";
    public static final String X_PAGOPA_PN_UID = "x-pagopa-pn-uid";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private InfoSelfcareGroupsService svc;

    @MockitoBean
    private InfoSelfcareUserService svcUser;

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
                .header(X_PAGOPA_PN_CX_ID, "internaluserid1234")
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
                .header(X_PAGOPA_PN_CX_ID, "internalUserId")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PgGroupDto.class)
                .hasSize(2);
    }

    @Test
    void getPgUserGroup(){
        String url = "/ext-registry-private/pg/v1/user-groups";

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
                .header(X_PAGOPA_PN_CX_ID, "cxId")
                .header(X_PAGOPA_PN_UID, "uid")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PgGroupDto.class)
                .hasSize(2);
    }

    @Test
    void getPgUser(){
        String url = "/ext-registry-private/pg/v1/user";

        PgUserDto dto1 = new PgUserDto();
        dto1.setId("id1");

        // When
        when(svcUser.getPgUserData("uid", "cxId")).thenReturn(Mono.just(dto1));

        // Then
        webTestClient.get()
                .uri(url)
                .header(X_PAGOPA_PN_CX_ID, "cxId")
                .header(X_PAGOPA_PN_UID, "uid")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PgUserDto.class);
    }

    @Test
    void getPgUsersDetailsPrivateReturnsUserDetails() {
        // Given
        String url = "/ext-registry-private/pg/v1/user-details";
        PgUserDetailDto userDetailDto = new PgUserDetailDto();
        userDetailDto.setId("id1");

        // When
        when(svcUser.getPgUserDetails("uid", "cxId")).thenReturn(Mono.just(userDetailDto));

        // Then
        webTestClient.get()
                .uri(url)
                .header(X_PAGOPA_PN_CX_ID, "cxId")
                .header(X_PAGOPA_PN_UID, "uid")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PgUserDetailDto.class)
                .isEqualTo(userDetailDto);
    }

    @Test
    void getPgUsersDetailsPrivateHandlesInternalServerError() {
        // Given
        String url = "/ext-registry-private/pg/v1/user-details";

        // When
        when(svcUser.getPgUserDetails("uid", "cxId")).thenReturn(Mono.error(new RuntimeException("Internal Server Error")));

        // Then
        webTestClient.get()
                .uri(url)
                .header(X_PAGOPA_PN_CX_ID, "cxId")
                .header(X_PAGOPA_PN_UID, "uid")
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

}
