package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageRequestDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.SendMessageResponseDto;
import it.pagopa.pn.external.registries.services.SendIOMessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = {SendIOMessageController.class})
class SendIOMessageControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    SendIOMessageService service;

    @Test
    void sendIOMessageSuccess() {
        //Given
        SendMessageRequestDto requestDto = new SendMessageRequestDto()
                .amount( 2000 )
                .creditorTaxId( "creditorTaxId" )
                .dueDate(OffsetDateTime.ofInstant( Instant.now(), ZoneId.of( "UTC" ) ))
                .iun( "iun" )
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .senderDenomination( "senderDenomination" )
                .subject( "subject" );

        SendMessageResponseDto responseDto = new SendMessageResponseDto()
                .id( "messageResponseId" );

        String url = "/ext-registry/io/v1/sendmessage";

        //When
        Mockito.when( service.sendIOMessage( Mockito.any() ) ).thenReturn( Mono.just( responseDto ) );

        //Then
        webTestClient.post()
                .uri( url )
                .body(Mono.just(requestDto), SendMessageResponseDto.class)
                .exchange()
                .expectStatus().isOk();
    }

}