package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = {SelfcarePgUserGroupClient.class, PnExternalRegistriesConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcarepgusergroup-base-url=http://localhost:9999"
})
class SelfcarePgUserGroupClientTest {

    @MockBean
    private PnExternalRegistriesConfig cfg;

    private static ClientAndServer mockServer;

    private SelfcarePgUserGroupClient client;

    @Configuration
    static class ContextConfiguration {
        @Bean
        @Primary
        public SqsNotificationPaidProducer sqsNotificationPaidProducer() {
            return mock(SqsNotificationPaidProducer.class);
        }
    }

    @BeforeEach
    void setup() {
        when(cfg.getSelfcarepgusergroupUid()).thenReturn("fake_sc_user");
        when(cfg.getSelfcarepgusergroupBaseUrl()).thenReturn("http://localhost:9999");
        client = new SelfcarePgUserGroupClient(cfg);
        client.init();
    }

    @BeforeAll
    public static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void getPgUserGroup() {
        UserGroupResourceDto groupDto = new UserGroupResourceDto()
                .name("name");

        PageOfUserGroupResourceDto responseDto = new PageOfUserGroupResourceDto()
                .content(List.of(groupDto));
        byte[] responseBytes = new byte[0];

        ObjectMapper mapper = new ObjectMapper();
        try {
            responseBytes = mapper.writeValueAsBytes(responseDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(request()
                        .withMethod("GET")
                        .withPath("/user-groups"))
                .respond(response()
                        .withBody(responseBytes)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));
            // When
            PageOfUserGroupResourceDto response = client.getUserGroups("id").block();

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals(responseDto.getContent().get(0).getName(), response.getContent().get(0).getName());
        }
    }
}