package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MockResponsesTest {

    private MockResponses service;

    private MockPa getMockedPa(String id, String name, String taxId, String email, String pec) {
        MockPa mockPa = new MockPa();
        mockPa.setId(id);
        mockPa.setName(name);
        mockPa.setTaxId(taxId);
        if (email != null || pec != null) {
            MockPaContacts mockPaContacts = new MockPaContacts();
            mockPaContacts.setEmail(email);
            mockPaContacts.setPec(pec);
            mockPa.setGeneralContacts(mockPaContacts);
        }
        return mockPa;
    }

    private List<MockPa> getMockedPaList() {
        List<MockPa> list = new ArrayList<>();
        list.add(getMockedPa("mocked-id", "mocked-name", "mocked-taxId", "mocked-mail", "mocked-pec"));
        list.add(getMockedPa("mocked-id2", "mocked-name2", "mocked-taxId2", null, null));
        list.add(getMockedPa("mocked-id3", "mocked-another-name", "mocked-taxId3", null, null));

        return list;
    }

    private MockDomicilie getMockedDomicile(String id, String address, String digitalAddress) {
        MockDomicilie mockDomicilie = new MockDomicilie();
        mockDomicilie.setId(id);
        mockDomicilie.setRecipientType("PF");
        MockAnalogDomicile mockAnalogDomicile = new MockAnalogDomicile();
        mockAnalogDomicile.setAddress(address);
        mockDomicilie.setAnalog(mockAnalogDomicile);
        MockDigitalDomicile mockDigitalDomicile = new MockDigitalDomicile();
        mockDigitalDomicile.setDomicileType("PEC");
        mockDomicilie.setDigital(mockDigitalDomicile);
        mockDigitalDomicile.setAddress(digitalAddress);
        return mockDomicilie;
    }

    private List<MockDomicilie> getMockedDomicileList() {
        List<MockDomicilie> list = new ArrayList<>();
        list.add(getMockedDomicile("mocked-id", "mocked-address", "mocked-digital-address"));
        list.add(getMockedDomicile("mocked-id2", "mocked-address2", "mocked-digital-address2"));

        return list;
    }

    private  List<PaSummaryDto> getPaSummary() {
        List<PaSummaryDto> paSummaryDtoList = new ArrayList<>();
        PaSummaryDto paSummaryDto = new PaSummaryDto();
        paSummaryDto.setName("mocked-name");
        paSummaryDto.setId("mocked-id");
        paSummaryDtoList.add(paSummaryDto);
        paSummaryDto = new PaSummaryDto();
        paSummaryDto.setName("mocked-name2");
        paSummaryDto.setId("mocked-id2");
        paSummaryDtoList.add(paSummaryDto);

        return paSummaryDtoList;
    }

    @BeforeAll
    public void setup() throws JsonProcessingException {
        PnExternalRegistriesConfig pnExternalRegistriesConfig = new PnExternalRegistriesConfig();
        pnExternalRegistriesConfig.setMockDataResources(JSONArray.toJSONString(getMockedPaList()));
        this.service = new MockResponses(pnExternalRegistriesConfig);
        this.service.setDomiciles(getMockedDomicileList());
    }

    @Test
    void getOnePaNotNull() {
        //GIVEN
        String id = "mocked-id";
        PaInfoDto paInfo = new PaInfoDto();
        paInfo.setId(id);
        paInfo.setName("mocked-name");
        paInfo.setTaxId("mocked-taxId");
        PaContactsDto paContactsDto = new PaContactsDto();
        paContactsDto.setEmail("mocked-mail");
        paContactsDto.setPec("mocked-pec");
        paInfo.setGeneralContacts(paContactsDto);

        // WHEN
        PaInfoDto res = service.getOnePa(id);

        //THEN
        assertNotNull(res);
        assertEquals(res, paInfo);
    }

    @Test
    void getOnePaNull() {
        //GIVEN
        String id = "mocked-not-existing-id";

        // WHEN
        PaInfoDto res = service.getOnePa(id);

        //THEN
        assertNull(res);
    }

    @Test
    void listOnboardedPaByName() {
        //GIVEN
        String name = "mocked-name";

        // WHEN
        List<PaSummaryDto> list = service.listOnboardedPa(name);

        //THEN
        assertFalse(list.isEmpty());
        assertEquals(list.size(), 2);
        assertEquals(list, getPaSummary());
    }

    @Test
    void listOnboardedPaByNameEmpty() {
        //GIVEN
        String name = "mocked-name-not-exist";

        // WHEN
        List<PaSummaryDto> list = service.listOnboardedPa(name);

        //THEN
        assertTrue(list.isEmpty());
    }

    @Test
    void listOnboardedPaById() {
        //GIVEN
        List<String> ids = new ArrayList<>();
        ids.add("mocked-id");
        ids.add("mocked-id2");

        // WHEN
        List<PaSummaryDto> list = service.listOnboardedPa(ids);

        //THEN
        assertFalse(list.isEmpty());
        assertEquals(list.size(), 2);
        assertEquals(list, getPaSummary());
    }

    @Test
    void listOnboardedPaByIdEmpty() {
        //GIVEN
        List<String> ids = new ArrayList<>();
        ids.add("mocked-id-not-exist");
        ids.add("mocked-id2-not-exist");

        // WHEN
        List<PaSummaryDto> list = service.listOnboardedPa(ids);

        //THEN
        assertTrue(list.isEmpty());
    }

    @Test
    void getOneAnalogDomicileNotNull() {
        //GIVEN
        String recipientType = "PF";
        String id = "mocked-id";
        AnalogDomicileDto analogDomicileDto = new AnalogDomicileDto();
        analogDomicileDto.setAddress("mocked-address");

        // WHEN
        AnalogDomicileDto res = service.getOneAnalogDomicile(recipientType, id);

        //THEN
        assertNotNull(res);
        assertEquals(res, analogDomicileDto);
    }

    @Test
    void getOneAnalogDomicileNull() {
        //GIVEN
        String recipientType = "PF";
        String id = "mocked-id-not-exist";

        // WHEN
        AnalogDomicileDto res = service.getOneAnalogDomicile(recipientType, id);

        //THEN
        assertNull(res);
    }

    @Test
    void getOneDigitalDomicileNotNull() {
        //GIVEN
        String recipientType = "PF";
        String id = "mocked-id";
        DigitalDomicileDto digitalDomicileDto = new DigitalDomicileDto();
        digitalDomicileDto.setAddress("mocked-digital-address");
        digitalDomicileDto.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.PEC);

        // WHEN
        DigitalDomicileDto res = service.getOneDigitalDomicile(recipientType, id);

        //THEN
        assertNotNull(res);
        assertEquals(res, digitalDomicileDto);
    }

    @Test
    void getOneDigitalDomicileNull() {
        //GIVEN
        String recipientType = "PF";
        String id = "mocked-id-not-exist";

        // WHEN
        DigitalDomicileDto res = service.getOneDigitalDomicile(recipientType, id);

        //THEN
        assertNull(res);
    }
}
