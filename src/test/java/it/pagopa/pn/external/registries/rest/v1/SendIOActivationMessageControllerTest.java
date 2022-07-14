package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendActivationMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.services.SendIOMessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {SendIOActivationMessageController.class})
class SendIOActivationMessageControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    SendIOMessageService service;

    @Test
    void sendIOActivationMessageSuccess() {
        //Given
        SendActivationMessageRequestDto requestDto = new SendActivationMessageRequestDto()
                .recipientTaxID( "recipientTaxId" );

        SendMessageResponseDto responseDto = new SendMessageResponseDto()
                .id( "messageResponseId" );

        String url = "/ext-registry-private/io/v1/sendactivationmessage";

        //When
        Mockito.when( service.sendIOActivationMessage( Mockito.any() ) ).thenReturn( Mono.just( responseDto ) );

        //Then
        webTestClient.post()
                .uri( url )
                .body(Mono.just(requestDto), SendMessageResponseDto.class)
                .exchange()
                .expectStatus().isOk();
    }

}