package it.pagopa.pn.external.registries.middleware.msclient.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseClientTest {

    private BaseClient client;

    @BeforeAll
    public void setup() {
        this.client = Mockito.mock(BaseClient.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void elabExceptionMessageNoMsg() {
        // GIVEN
        Throwable exp = new Throwable();

        // WHEN
        String msg = client.elabExceptionMessage(exp);

        // THEN
        assertEquals("", msg);
    }

    @Test
    void elabExceptionMessageMsg() {
        // GIVEN
        Throwable exp = new Throwable("fake message");

        // WHEN
        String msg = client.elabExceptionMessage(exp);

        // THEN
        assertEquals("fake message", msg);
    }

    @Test
    void elabExceptionMessageWebClient() {
        // GIVEN
        Throwable exp = WebClientResponseException.create(404, "fake message", HttpHeaders.EMPTY, new byte[0], null);

        // WHEN
        String msg = client.elabExceptionMessage(exp);

        // THEN
        assertEquals("404 fake message;", msg);
    }
}
