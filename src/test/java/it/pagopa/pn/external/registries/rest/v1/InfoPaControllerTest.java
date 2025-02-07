package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.services.InfoSelfcareGroupsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import it.pagopa.pn.external.registries.services.InfoSelfcareUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@WebFluxTest(controllers = {InfoPaController.class})
class InfoPaControllerTest {

    public static final String PN_PAGOPA_USER_ID = "x-pagopa-pn-cx-id";
    public static final String PN_PAGOPA_UID = "x-pagopa-pn-uid";
    public static final String PN_PAGOPA_GROUPS = "x-pagopa-pn-cx-groups";
    public static final String PN_PAGOPA_SRC_CH = "x-pagopa-pn-src-ch";
    public static final String PN_PAGOPA_USER_TYPE = "x-pagopa-pn-cx-type";
    public static final String PN_PAGOPA_SRC_CH_TYPE = "x-pagopa-pn-src-ch-details";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InfoSelfcareGroupsService svc;

    @MockBean
    private InfoSelfcareInstitutionsService svcInst;

    @MockBean
    private InfoSelfcareUserService svcUser;

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
        String url = "/ext-registry/pa/v1/activated-on-pn?id={id}"
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
    void extendedListOnboardedPa() {
        // Given
        String url = "/ext-registry/pa/v2/activated-on-pn?page={page}&size={size}"
                .replace("{page}", "1")
                .replace("{size}", "10");

        PaSummaryExtendedResponseDto responseDto = getPaSummaryExtendedResponseDto();

        // When
        Mockito.when(svcInst.extendedListOnboardedPaByName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(responseDto));

        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaSummaryExtendedResponseDto.class)
                .value(resp -> {
                    Assertions.assertNotNull(resp);
                    Assertions.assertEquals(2, resp.getContent().size());
                    Assertions.assertEquals("Regione Lazio", resp.getContent().get(0).getName());
                    Assertions.assertEquals("Consiglio Regionale del Lazio", resp.getContent().get(1).getName());
                });
    }

    @Test
    void extendedListOnboardedPa_withNameFilter() {
        // Given
        String url = "/ext-registry/pa/v2/activated-on-pn?paNameFilter={paNameFilter}&page={page}&size={size}"
                .replace("{paNameFilter}", "Lazio")
                .replace("{page}", "1")
                .replace("{size}", "10");

        PaSummaryExtendedResponseDto responseDto = getPaSummaryExtendedResponseDto();
        responseDto.getContent().remove(1);

        // When
        Mockito.when(svcInst.extendedListOnboardedPaByName(Mockito.eq("Lazio"), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(responseDto));

        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaSummaryExtendedResponseDto.class)
                .value(resp -> {
                    Assertions.assertNotNull(resp);
                    Assertions.assertEquals(1, resp.getContent().size());
                    Assertions.assertEquals("Regione Lazio", resp.getContent().get(0).getName());
                });
    }

    @Test
    void extendedListOnboardedPa_onlyChildren() {
        // Given
        String url = "/ext-registry/pa/v2/activated-on-pn?onlyChildren={onlyChildren}&page={page}&size={size}"
                .replace("{onlyChildren}", "1")
                .replace("{page}", "1")
                .replace("{size}", "10");

        PaSummaryExtendedResponseDto responseDto = getPaSummaryExtendedResponseDto();
        responseDto.getContent().remove(0);

        // When
        Mockito.when(svcInst.extendedListOnboardedPaByName(Mockito.any(), Mockito.eq(true), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(responseDto));

        // Then
        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaSummaryExtendedResponseDto.class)
                .value(resp -> {
                    Assertions.assertNotNull(resp);
                    Assertions.assertEquals("Consiglio Regionale del Lazio", resp.getContent().get(0).getName());
                    Assertions.assertEquals(1, resp.getContent().size());
                    Assertions.assertEquals("18346279", resp.getContent().get(0).getChildrenList().get(0).getId());
                    Assertions.assertEquals("Ufficio tributi Roma", resp.getContent().get(0).getChildrenList().get(0).getName());
                });
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
        Mockito.when(svc.getPaGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
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

    @Test
    void getInstitutions() {

        // Given
        String url = "/ext-registry/pa/v1/institutions";

        List<InstitutionResourcePNDto> res = new ArrayList<>();
        InstitutionResourcePNDto dto = new InstitutionResourcePNDto();
        dto.setAddress("Via vittorio veneto, 23");
        dto.setDescription("Comune di Milano");
        dto.setDigitalAddress("xxx@cert.xxx.it");
        dto.setExternalId("00431230123");
        dto.setId(UUID.randomUUID());
        dto.setInstitutionType(InstitutionResourcePNDto.InstitutionTypeEnum.PA);
        dto.setZipCode("12345");
        dto.setTaxCode("00431230123");
        dto.setStatus("ACTIVE");
        List<String> userProductRoles = new ArrayList<>();
        userProductRoles.add("admin");
        dto.setUserProductRoles(userProductRoles);
        res.add(dto);
        dto.setId(UUID.randomUUID());
        res.add(dto);


        // When
        Mockito.when(svcInst.listInstitutionByCurrentUser(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyList(),Mockito.anyString()))
                .thenReturn(Flux.fromIterable(res));

        // Then
        webTestClient.get()
                .uri(url)
                .header( PN_PAGOPA_GROUPS, "")
                .header( PN_PAGOPA_USER_ID, "internaluserid1234")
                .header( PN_PAGOPA_UID, "PA-internaluserid1234")
                .header( PN_PAGOPA_SRC_CH, "WEB")
                .header( PN_PAGOPA_USER_TYPE, "PA")
                .header( PN_PAGOPA_SRC_CH_TYPE, "PA")
                .exchange()
                .expectStatus().isOk().expectBodyList(InstitutionResourcePNDto.class).hasSize(2);
    }

    @Test
    void getUserInstitutions() {

        // Given
        String url = "/ext-registry/pa/v1/user-institutions";

        List<InstitutionResourcePNDto> res = new ArrayList<>();
        InstitutionResourcePNDto dto = new InstitutionResourcePNDto();
        dto.setAddress("Via vittorio veneto, 23");
        dto.setDescription("Comune di Milano");
        dto.setDigitalAddress("xxx@cert.xxx.it");
        dto.setExternalId("00431230123");
        dto.setId(UUID.randomUUID());
        dto.setInstitutionType(InstitutionResourcePNDto.InstitutionTypeEnum.PA);
        dto.setZipCode("12345");
        dto.setTaxCode("00431230123");
        dto.setStatus("ACTIVE");
        List<String> userProductRoles = new ArrayList<>();
        userProductRoles.add("admin");
        dto.setUserProductRoles(userProductRoles);
        res.add(dto);
        dto.setId(UUID.randomUUID());
        res.add(dto);


        // When
        Mockito.when(svcUser.listUserInstitutionByCurrentUser(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyList(),Mockito.anyString()))
                .thenReturn(Flux.fromIterable(res));

        // Then
        webTestClient.get()
                .uri(url)
                .header( PN_PAGOPA_GROUPS, "")
                .header( PN_PAGOPA_USER_ID, "internaluserid1234")
                .header( PN_PAGOPA_UID, "PA-internaluserid1234")
                .header( PN_PAGOPA_SRC_CH, "WEB")
                .header( PN_PAGOPA_USER_TYPE, "PA")
                .header( PN_PAGOPA_SRC_CH_TYPE, "PA")
                .exchange()
                .expectStatus().isOk().expectBodyList(InstitutionResourcePNDto.class).hasSize(2);
    }


    @Test
    void getInstitutionProducts() {

        // Given
        String url = "/ext-registry/pa/v1/institutions/{id}/products"
                .replace("{id}", UUID.randomUUID().toString());

        List<ProductResourcePNDto> res = new ArrayList<>();
        ProductResourcePNDto dto = new ProductResourcePNDto();
        dto.setCreatedAt(new Date());
        dto.setDescription("Comune di Milano");
        dto.setId("test-pn");
        dto.setLogo("http://test.com/logo.csv");
        dto.setLogoBgColor("#0066CC");
        dto.setIdentityTokenAudience("identityToken");
        dto.setTitle("SEND - Servizio Notifiche Digitali");
        res.add(dto);
        res.add(dto);


        // When
        Mockito.when(svcInst.listProductsByInstitutionAndCurrentUser(Mockito.anyString(), Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyList(),Mockito.anyString()))
                .thenReturn(Flux.fromIterable(res));

        // Then
        webTestClient.get()
                .uri(url)
                .header( PN_PAGOPA_GROUPS, "")
                .header( PN_PAGOPA_USER_ID, "internaluserid1234")
                .header( PN_PAGOPA_UID, "PA-internaluserid1234")
                .header( PN_PAGOPA_SRC_CH, "WEB")
                .header( PN_PAGOPA_USER_TYPE, "PA")
                .header( PN_PAGOPA_SRC_CH_TYPE, "PA")
                .exchange()
                .expectStatus().isOk().expectBodyList(ProductResourcePNDto.class).hasSize(2);
    }

    private PaSummaryExtendedResponseDto getPaSummaryExtendedResponseDto() {
        PaSummaryExtendedResponseDto responseDto = new PaSummaryExtendedResponseDto();
        responseDto.setTotalPages(1L);
        responseDto.setTotalElements(2L);

        List<PaSummaryExtendedDto> content = new ArrayList<>();

        PaSummaryExtendedDto father1 = new PaSummaryExtendedDto();
        father1.setId("123456789");
        father1.setName("Regione Lazio");

        PaSummaryExtendedDto father2 = new PaSummaryExtendedDto();
        father2.setId("987654321");
        father2.setName("Consiglio Regionale del Lazio");

        PaSummaryExtendedInfoDto child = new PaSummaryExtendedInfoDto();
        child.setId("18346279");
        child.setName("Ufficio tributi Roma");
        father2.setChildrenList(List.of(child));

        content.add(father1);
        content.add(father2);

        responseDto.setContent(content);
        return responseDto;
    }
}