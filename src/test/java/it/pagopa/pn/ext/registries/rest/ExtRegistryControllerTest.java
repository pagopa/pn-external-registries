package it.pagopa.pn.ext.registries.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.ext.registries.pa.model.PaContactsDto;
import it.pagopa.pn.ext.registries.pa.model.PaInfoDto;
import it.pagopa.pn.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class ExtRegistryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static PaInfoDto paInfoDto;
    @Autowired private ObjectMapper objectMapper;
    private JacksonTester<PaContactsDto> paContactsDtoJsonTester;

    private static String paId;

    @BeforeAll
    public static void setup() {

        paId = "c_f205";
        PaContactsDto pac = new PaContactsDto("protocollo@postacert.comune.milano.it",
                "protocollo@comune.milano.it",
                "340000333",
                URI.create("www.comune.milano.it"));
        paInfoDto = new PaInfoDto(paId,"Comune di Milano","01199250158",pac);
        paInfoDto.setGeneralContacts(pac);
    }

    @Test
    void getOnePa_Ok()  throws Exception {

        String content = TestUtils.toJson(paInfoDto);

        this.mockMvc
                .perform(
                       get("/ext-registry/pa/v1/activated-on-pn/" + paId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(content));
     }

    @Test
    void getOnePa_Bad_Request()  throws Exception {

        String id = "c_xxxx";

        this.mockMvc
                .perform(
                        get("/ext-registry/pa/v1/activated-on-pn/" + id)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
        ;
    }

    @Test
    void listOnboardedPa_OK_lista_non_vuota()  throws Exception {

        String content = "[" + TestUtils.toJson(paInfoDto) + "]";

        this.mockMvc
                .perform(
                        get("/ext-registry/pa/v1/activated-on-pn?paNameFilter=c_f")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(content));
    }

    @Test
    void listOnboardedPa_OK_lista_vuota()  throws Exception {

        this.mockMvc
                .perform(
                        get("/ext-registry/pa/v1/activated-on-pn?paNameFilter=c_xxx")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // parametro paNameFilter Invalido
    @Test
    void listOnboardedPa_Validation_Error()  throws Exception {

        this.mockMvc
                .perform(
                        get("/ext-registry/pa/v1/activated-on-pn?paNameFilter=c_xxx$")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

}