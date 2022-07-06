package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.services.MVPValidUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = {MVPValidUserController.class})
class MVPValidUserControllerTest {

    public static final String TAX_ID = "EEEEEE00E00E000A";
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MVPValidUserService service;

    @Test
    void checkMvpUserSuccess() {
        // Given
        MvpUserDto mvpUserDto = new MvpUserDto()
                .valid( true )
                .taxId( TAX_ID );

        String url = "/ext-registry-b2b/pa/v1/mvp-valid-users";

        // When
        Mockito.when( service.checkValidUser( Mockito.any() ) ).thenReturn( Mono.just( mvpUserDto ) );

        // Then
        webTestClient.post()
                .uri( url )
                .contentType( MediaType.APPLICATION_JSON )
                .body(Mono.just( TAX_ID ), String.class)
                .exchange()
                .expectStatus().isOk();
    }

}