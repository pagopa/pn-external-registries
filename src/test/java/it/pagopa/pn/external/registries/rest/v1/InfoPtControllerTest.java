package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtDelegationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.server.pt.v1.dto.PtInstitutionsResponseDto;
import it.pagopa.pn.external.registries.services.InfoPtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {InfoPtController.class})
class InfoPtControllerTest {

    private static final String URL = "/ext-registry-private/pt/v1/institutions";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InfoPtService infoPtService;

    @Test
    void getPtInstitutions() throws JsonProcessingException {
        PtInstitutionDto institution = new PtInstitutionDto();
        institution.setInstitutionId("ente-B");
        institution.setInstitutionName("Comune di Bari");
        institution.setInstitutionTaxCode("22222222222");
        institution.setDelegationId("delegation-ente-B");
        institution.setStatus(PtDelegationStatusDto.ACTIVE);
        PtInstitutionsResponseDto expectedResponse = new PtInstitutionsResponseDto();
        expectedResponse.setItems(List.of(institution));
        expectedResponse.setLastKey("ente-B");

        when(infoPtService.getPtInstitutions("pt-1", "bari", PtDelegationStatusDto.ACTIVE, 5, "ente-C"))
                .thenReturn(Mono.just(expectedResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(URL)
                        .queryParam("search", "bari")
                        .queryParam("status", "ACTIVE")
                        .queryParam("limit", 5)
                        .queryParam("lastKey", "ente-C")
                        .build())
                .headers(this::contextHeaders)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    void getPtInstitutionsWithDefaultLimit() {
        when(infoPtService.getPtInstitutions("pt-1", null, null, 10, null))
                .thenReturn(Mono.just(new PtInstitutionsResponseDto()));

        webTestClient.get()
                .uri(URL)
                .headers(this::contextHeaders)
                .exchange()
                .expectStatus().isOk();

        verify(infoPtService).getPtInstitutions("pt-1", null, null, 10, null);
    }

    @Test
    void getPtInstitutionsWithInvalidLimit() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(URL).queryParam("limit", 51).build())
                .headers(this::contextHeaders)
                .exchange()
                .expectStatus().isBadRequest();

        verify(infoPtService, never()).getPtInstitutions(anyString(), any(), any(), anyInt(), any());
    }

    @Test
    void getPtInstitutionsWithTooShortSearch() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(URL).queryParam("search", "ba").build())
                .headers(this::contextHeaders)
                .exchange()
                .expectStatus().isBadRequest();

        verify(infoPtService, never()).getPtInstitutions(anyString(), any(), any(), anyInt(), any());
    }

    private void contextHeaders(HttpHeaders headers) {
        headers.add("x-pagopa-pn-cx-id", "pt-1");
        headers.add("x-pagopa-pn-cx-type", "PT");
        headers.add("x-pagopa-pn-uid", "user-1");
        headers.add("x-pagopa-pn-src-ch", "WEB");
    }
}
