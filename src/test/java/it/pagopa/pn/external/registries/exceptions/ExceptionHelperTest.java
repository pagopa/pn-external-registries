package it.pagopa.pn.external.registries.exceptions;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.ProblemDto;
import org.apache.http.protocol.HTTP;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHelperTest {

    @Test
    void getHttpStatusFromException() {
        HttpStatus status = ExceptionHelper.getHttpStatusFromException(new NotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, status);
    }


    @Test
    void handlePnException() {

        //When
        ProblemDto res = ExceptionHelper.handleException(new NotFoundException(), HttpStatus.NOT_FOUND);

        //Then
        assertNotNull(res);
        assertEquals("Elemento non trovato", res.getTitle());
        assertEquals(404, res.getStatus());
    }


    @Test
    void handlePnExceptionInternal() {

        //When
        ProblemDto res = ExceptionHelper.handleException(new InternalErrorException(), HttpStatus.INTERNAL_SERVER_ERROR);

        //Then
        assertNotNull(res);
        assertEquals("Errore interno", res.getTitle());
        assertEquals(500, res.getStatus());
    }


    @Test
    void handlePnExceptionAssertionGeneration() {

        //When
        ProblemDto res = ExceptionHelper.handleException(new AssertionGeneratorException(new NullPointerException()), HttpStatus.INTERNAL_SERVER_ERROR);

        //Then
        assertNotNull(res);
        assertEquals("Errore interno", res.getTitle());
        assertEquals(500, res.getStatus());
    }


    @Test
    void handleException() {

        //When
        ProblemDto res = ExceptionHelper.handleException(new NullPointerException(), HttpStatus.BAD_REQUEST);

        //Then
        assertNotNull(res);
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.getStatus());
    }
}