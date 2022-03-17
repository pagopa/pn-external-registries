package it.pagopa.pn.ext.registries.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.ext.registries.pa.model.PaContactsDto;
import it.pagopa.pn.ext.registries.pa.model.PaInfoDto;
import it.pagopa.pn.ext.registries.privati.model.AnalogDomicileDto;
import it.pagopa.pn.ext.registries.privati.model.DigitalDomicileDto;
import it.pagopa.pn.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class ExtRegistryPrivatoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static PaInfoDto paInfoDto;
    @Autowired private ObjectMapper objectMapper;
    private JacksonTester<PaContactsDto> paContactsDtoJsonTester;

    private static AnalogDomicileDto analogDomicileDto;
    private static DigitalDomicileDto digitalDomicileDto;

    @BeforeAll
    static void setUp() {
        analogDomicileDto = new AnalogDomicileDto();
        analogDomicileDto.setAddress("ImmediateResponse(OK)");
        analogDomicileDto.setCap("40100");
        analogDomicileDto.setMunicipality("Bologna");
        analogDomicileDto.setProvince("BO");

        digitalDomicileDto = new DigitalDomicileDto();
        digitalDomicileDto.setAddress("nome.cognome.1@works.pec.it");
        digitalDomicileDto.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.PEC);
    }

    @Test
    void getOneAnalogDomicile_OK()  throws Exception {
        String content = TestUtils.toJson(analogDomicileDto);

        String recipientType = "PF";
        String opaqueId = "c0a235b2-a454-11ec-b909-0242ac120002";
        String url = String.format("/ext-registry-private/domiciles/v1/%s/%s/analog", recipientType, opaqueId);

        this.mockMvc
                .perform(
                        get(url)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(content));
    }

    @Test
    void getOneDigitalDomicile_OK()  throws Exception {
        String content = TestUtils.toJson(digitalDomicileDto);

        String recipientType = "PF";
        String opaqueId = "c0a235b2-a454-11ec-b909-0242ac120002";
        String url = String.format("/ext-registry-private/domiciles/v1/%s/%s/digital", recipientType, opaqueId);

        this.mockMvc
                .perform(
                        get(url)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(content));

    }

    @Test
    void getOneDigitalDomicile_KO_Bad_recipientType()  throws Exception {
        String content = TestUtils.toJson(digitalDomicileDto);

        String recipientType = "PFXX";
        String opaqueId = "c0a235b2-a454-11ec-b909-0242ac120002";
        String url = String.format("/ext-registry-private/domiciles/v1/%s/%s/digital", recipientType, opaqueId);

        this.mockMvc
                .perform(
                        get(url)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOneDigitalDomicile_KO_Bad_opaqueId()  throws Exception {
        String content = TestUtils.toJson(digitalDomicileDto);

        String recipientType = "PF";
        String opaqueId = "c0a235b2-a454-11ec-b909-0242ac120002_XXXX";
        String url = String.format("/ext-registry-private/domiciles/v1/%s/%s/digital", recipientType, opaqueId);

        this.mockMvc
                .perform(
                        get(url)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}