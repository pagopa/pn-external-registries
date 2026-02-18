package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.exceptions.PnRootIdNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.RootSenderIdResponseDto;
import it.pagopa.pn.external.registries.services.InfoSelfcareInstitutionsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(RootSenderIdController.class)
class RootSenderIdControllerTest {
    private static final String URL = "/ext-registry-private/pa/v1/{senderId}/root-id";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private InfoSelfcareInstitutionsService infoSelfcareInstitutionsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRootIdOK() throws JsonProcessingException {

        final String ROOT_ID = "parent-root-id";
        RootSenderIdResponseDto expected = new RootSenderIdResponseDto().rootId(ROOT_ID);

        // When
        Mockito.when( infoSelfcareInstitutionsService.getRootId(Mockito.anyString()))
            .thenReturn( Mono.just( new RootSenderIdResponseDto().rootId(ROOT_ID)) );

        // Then
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path(URL).build("sender-id"))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .json(objectMapper.writeValueAsString(expected));
    }

    @Test
    void getRootIdKO() {

        // When
        Mockito.when( infoSelfcareInstitutionsService.getRootId(Mockito.anyString()))
            .thenReturn( Mono.error( new PnRootIdNotFoundException("root id not found")));

        // Then
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path(URL).build("sender-id"))
            .exchange()
            .expectStatus()
            .isNotFound();
    }
}
