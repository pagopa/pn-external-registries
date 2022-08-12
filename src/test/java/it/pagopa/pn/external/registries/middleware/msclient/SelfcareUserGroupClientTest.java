package it.pagopa.pn.external.registries.middleware.msclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.utils.AssertionGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.external-registry.selfcareusergroup-base-url=http://localhost:9999"
})
class SelfcareUserGroupClientTest {

    @Autowired
    private SelfcareUserGroupClient client;

    @MockBean
    private AssertionGenerator assertionGenerator;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }


    @Test
    void getUserGroups() {

        UserGroupResourceDto userDto = new UserGroupResourceDto()
                .name("companyname");

        List<UserGroupResourceDto> list = new ArrayList<>();
        list.add(userDto);

        byte[] responseBodyBites = new byte[0];

        PageOfUserGroupResourceDto responseDto = new PageOfUserGroupResourceDto();
        responseDto.setContent(list);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerFor( new TypeReference<List<UserGroupResourceDto>>(){});
        try {
            responseBodyBites = mapper.writeValueAsBytes( responseDto );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withPath("/user-groups"))
                .respond(response()
                        .withBody(responseBodyBites)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        PageOfUserGroupResourceDto response = client.getUserGroups("77777777777302000100000019421").block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals(1, response.getContent().size());
        Assertions.assertEquals( responseDto.getContent().get(0).getName() , response.getContent().get(0).getName() );
    }
}
