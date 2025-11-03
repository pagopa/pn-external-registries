package it.pagopa.pn.external.registries.rest.io.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.exceptions.PnNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.PreconditionContentDto;
import it.pagopa.pn.external.registries.services.io.IOService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.util.AppIOUtils.POST_ANALOG_TITLE;

@WebFluxTest(controllers = {FromIOMessageController.class})
class FromIOMessageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOService service;

    @Test
    void notificationDisclaimerSuccess() throws JsonProcessingException {
        //Given
        final String xPagopaPnCxId = "recipientInternalId";
        final String iun = "iun";

        String url = "/ext-registry-private/io/v1/notification-disclaimer/{iun}";

        //When
        PreconditionContentDto response = new PreconditionContentDto()
                .title(POST_ANALOG_TITLE)
                .markdown("markdown");

        Mockito.when(service.notificationDisclaimer(xPagopaPnCxId, iun)).thenReturn(Mono.just(response));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(url).build(iun))
                .header("x-pagopa-pn-cx-id", xPagopaPnCxId)
                .exchange()
                .expectStatus().isOk().expectBody().json(objectMapper.writeValueAsString(response));
    }

    @Test
    void notificationDisclaimerBadRequest() {
        //Given
        final String xPagopaPnCxId = "recipientInternalId";
        final String iun = "iun";

        String url = "/ext-registry-private/io/v1/notification-disclaimer/{iun}";

        //When
        PreconditionContentDto response = new PreconditionContentDto()
                .title(POST_ANALOG_TITLE)
                .markdown("markdown");

        Mockito.when(service.notificationDisclaimer(xPagopaPnCxId, iun)).thenReturn(Mono.just(response));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(url).build(iun))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void notificationDisclaimerNotFound() {
        //Given
        final String xPagopaPnCxId = "recipientInternalId";
        final String iun = "iun";

        String url = "/ext-registry-private/io/v1/notification-disclaimer/{iun}";

        //When
        Mockito.when(service.notificationDisclaimer(xPagopaPnCxId, iun)).thenReturn(Mono.error(new PnNotFoundException("", "", "")));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(url).build(iun))
                .header("x-pagopa-pn-cx-id", xPagopaPnCxId)
                .exchange()
                .expectStatus().isNotFound();
    }


}
