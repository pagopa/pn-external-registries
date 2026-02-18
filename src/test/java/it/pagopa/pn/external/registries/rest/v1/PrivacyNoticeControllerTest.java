package it.pagopa.pn.external.registries.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.exceptions.PnPrivacyNoticeNotFound;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.external.registries.services.PrivacyNoticeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(PrivacyNoticeController.class)
class PrivacyNoticeControllerTest {

    private static final String URL = "/ext-registry-private/privacynotice/{consentsType}/{portalType}";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private PrivacyNoticeService privacyNoticeService;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void getPrivacyNoticeVersionOK() throws JsonProcessingException {

        PrivacyNoticeVersionResponseDto expected = new PrivacyNoticeVersionResponseDto().version(1);

        // When
        Mockito.when( privacyNoticeService.findPrivacyNoticeVersion("TOS","PF"))
                .thenReturn( Mono.just( new PrivacyNoticeVersionResponseDto().version(1)) );

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(URL).build("TOS", "PF"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(expected));
    }

    @Test
    void getPrivacyNoticeVersionKO() {

        // When
        Mockito.when( privacyNoticeService.findPrivacyNoticeVersion("TOS","PF"))
                .thenReturn( Mono.error( new PnPrivacyNoticeNotFound("Not found")) );

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(URL).build("TOS", "PF"))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

}
