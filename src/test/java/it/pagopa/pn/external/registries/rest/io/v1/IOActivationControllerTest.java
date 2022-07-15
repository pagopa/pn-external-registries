package it.pagopa.pn.external.registries.rest.io.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.rest.io.v1.IOActivationController;
import it.pagopa.pn.external.registries.services.io.IOActivationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {IOActivationController.class})
class IOActivationControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    IOActivationService service;

    @Test
    void getServiceActivationByPOST() {
        //Given
        FiscalCodePayloadDto requestDto = new FiscalCodePayloadDto();
        requestDto.setFiscalCode("EEEEEE00E00E000A" );

        ActivationDto responseDto = new ActivationDto();
        responseDto.setFiscalCode( "EEEEEE00E00E000A" );
        responseDto.setVersion(1);
        responseDto.setStatus(ActivationStatusDto.ACTIVE);

        String url = "/ext-registry-private/io/v1/activations";

        //When
        Mockito.when( service.getServiceActivation( Mockito.any() ) ).thenReturn( Mono.just( responseDto ) );

        //Then
        webTestClient.post()
                .uri( url )
                .body(Mono.just(requestDto), SendMessageResponseDto.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void upsertServiceActivation() {
        //Given
        ActivationPayloadDto requestDto = new ActivationPayloadDto();
        requestDto.setFiscalCode("EEEEEE00E00E000A" );
        requestDto.setStatus(ActivationStatusDto.ACTIVE);

        ActivationDto responseDto = new ActivationDto();
        responseDto.setFiscalCode( "EEEEEE00E00E000A" );
        responseDto.setVersion(1);
        responseDto.setStatus(ActivationStatusDto.ACTIVE);

        String url = "/ext-registry-private/io/v1/activations";

        //When
        Mockito.when( service.upsertServiceActivation( Mockito.any() ) ).thenReturn( Mono.just( responseDto ) );

        //Then
        webTestClient.put()
                .uri( url )
                .body(Mono.just(requestDto), SendMessageResponseDto.class)
                .exchange()
                .expectStatus().isOk();
    }
}