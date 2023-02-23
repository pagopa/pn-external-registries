package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
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

    public static final String PN_PAGOPA_USER_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_UID = "x-pagopa-pn-uid";
    public static final String PN_PAGOPA_GROUPS = "x-pagopa-pn-cx-groups";


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareGroupsService svc;

    @MockBean
    private InfoSelfcareInstitutionsService svcInst;

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
        Mockito.when(svcInst.getOnePa(Mockito.anyString()))
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
        Mockito.when(svcInst.listOnboardedPaByName(Mockito.any()))
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
        Mockito.when(svcInst.listOnboardedPaByIds(Mockito.anyList()))
                .thenReturn(Flux.fromIterable(res));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk().expectBodyList(PaSummaryDto.class).hasSize(1);
    }


    @Test
    void getGroups() {

        // Given
        String url = "/ext-registry/pa/v1/groups";

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
                .header( PN_PAGOPA_UID, "PF-internaluserid1234")
                .header( PN_PAGOPA_GROUPS, "")
                .exchange()
                .expectStatus().isOk().expectBodyList(PaGroupDto.class).hasSize(2);
    }

    @Test
    void getManyPa() {

        // Given
        String url = "/ext-registry-private/pa/v1/activated-on-pn?id={id1}&id={id2}"
                .replace("{id1}", "123456789")
                .replace("{id2}", "223456789");

        List<PaSummaryDto> res = new ArrayList<>();
        PaSummaryDto dto = new PaSummaryDto();
        dto.setId("123456789");
        dto.setName("pubblica amministrazione XXX");
        res.add(dto);
        dto = new PaSummaryDto();
        dto.setId("223456789");
        dto.setName("pubblica amministrazione XXX");
        res.add(dto);


        // When
        Mockito.when(svcInst.listOnboardedPaByIds(Mockito.anyList()))
                .thenReturn(Flux.fromIterable(res));


        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk().expectBodyList(PaSummaryDto.class).hasSize(2);
    }
}