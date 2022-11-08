package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InfoPapiImplTest {
    private final Duration d = Duration.ofMillis(3000);

    private InfoPapiImpl service;

    private MockResponses mrh;

    @BeforeEach
    void setup(){
        mrh = Mockito.mock(MockResponses.class);
        PnExternalRegistriesConfig pnExternalRegistriesConfig = Mockito.mock(PnExternalRegistriesConfig.class);
        this.service = new InfoPapiImpl( mrh, pnExternalRegistriesConfig);
    }

    @Test
    void getOnePaNotNull() {
        //GIVEN
        String id = "mocked-id";
        PaInfoDto paInfo = new PaInfoDto();
        paInfo.setId(id);
        paInfo.setName("mocked-name");
        paInfo.setTaxId("mocked-taxId");

        // WHEN
        Mockito.when(mrh.getOnePa(id)).thenReturn(paInfo);
        PaInfoDto res = service.getOnePa(id).block(d);

        //THEN
        assertNotNull(res);
        assertEquals(res, paInfo);
    }

    @Test
    void getOnePaNull() {
        //GIVEN
        String id = "mocked-id";

        // WHEN
        Mockito.when(mrh.getOnePa(id)).thenReturn(null);

        //THEN
        assertThrows(PnPANotFoundException.class, () -> service.getOnePa(id));
    }

    @Test
    void listOnboardedPaByName() {
        //GIVEN
        String name = "mocked-name";
        PaSummaryDto paSummaryDto = new PaSummaryDto();
        paSummaryDto.setId("mocked-id");
        paSummaryDto.setName(name);
        List<PaSummaryDto> list = new ArrayList<>();
        list.add(paSummaryDto);

        // WHEN
        Mockito.when(mrh.listOnboardedPa(name)).thenReturn(list);
        List<PaSummaryDto> res = service.listOnboardedPaByName(name).collectList().block(d);

        //THEN
        assertNotNull(res);
        assertEquals(res, list);
    }

    @Test
    void listOnboardedPaByIds() {
        //GIVEN
        List<String> ids = new ArrayList<>();
        ids.add("mocked-id");
        ids.add("mocked-id-2");

        PaSummaryDto paSummaryDto = new PaSummaryDto();
        paSummaryDto.setId("mocked-id");
        paSummaryDto.setName("mocked-name");
        List<PaSummaryDto> list = new ArrayList<>();
        list.add(paSummaryDto);
        paSummaryDto = new PaSummaryDto();
        paSummaryDto.setId("mocked-id-2");
        paSummaryDto.setName("mocked-name-2");
        list.add(paSummaryDto);

        // WHEN
        Mockito.when(mrh.listOnboardedPa(ids)).thenReturn(list);
        List<PaSummaryDto> res = service.listOnboardedPaByIds(ids).collectList().block(d);

        //THEN
        assertNotNull(res);
        assertEquals(res, list);
    }
}


