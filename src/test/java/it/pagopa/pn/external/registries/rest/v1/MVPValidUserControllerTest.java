package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.services.MVPValidUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


@WebFluxTest(controllers = {MVPValidUserController.class})
class MVPValidUserControllerTest {
    private static final String TAX_ID = "EEEEEE00E00E000A";
    private static final String url = "/ext-registry-b2b/pa/v1/mvp-valid-users";
    
    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    MVPValidUserService service;

    @Test
    void checkMvpUserPnActive() {
        // Given
        MvpUserDto mvpUserDto = new MvpUserDto()
                .valid(true)
                .taxId( TAX_ID );


        // When
        Mockito.when( service.checkValidUser( Mockito.any() ) ).thenReturn( Mono.just( mvpUserDto ) );

        // Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(TAX_ID), String.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MvpUserDto.class).consumeWith(
                        elem -> {
                            MvpUserDto dto = elem.getResponseBody();
                            assert dto != null;
                            Assertions.assertTrue(mvpUserDto.getValid());
                            Assertions.assertEquals(mvpUserDto.getTaxId(), dto.getTaxId());
                        }
                );
    }

}