package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InfoDomicilieImplTest {
    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private InfoDomicilieImpl service;

    @Test
    void getOneAnalogDomicile() {
        //GIVEN
        UUID id = UUID.fromString("c0a235b2-a454-11ec-b909-0242ac120002");


        // WHEN
        AnalogDomicileDto res = service.getOneAnalogDomicile(RecipientTypeDto.PF, id).block(d);


        //THEN
        assertNotNull(res);
        assertEquals("ITALIA", res.getState());
        assertEquals("da compilare", res.getAt());
        assertEquals("ImmediateResponse(OK)", res.getAddress());
        assertEquals("40100", res.getCap());
        assertEquals("Bologna", res.getMunicipality());
        assertEquals("BO", res.getProvince());
        assertEquals("Via dei pini, 14", res.getAddressDetails());
    }


    @Test
    void getOneAnalogDomicileNotFound() {
        //GIVEN
        UUID id = UUID.fromString("d0a235b2-a454-11ec-b909-0242ac120002");


        // WHEN
        assertThrows(NotFoundException.class, () -> service.getOneAnalogDomicile(RecipientTypeDto.PF, id));
        //THEN
    }

    @Test
    void getOneDigitalDomicile() {
        //GIVEN
        UUID id = UUID.fromString("c0a235b2-a454-11ec-b909-0242ac120002");

        // WHEN
        DigitalDomicileDto res = service.getOneDigitalDomicile(RecipientTypeDto.PF, id).block(d);

        //THEN
        assertNotNull(res);
        assertEquals("nome.cognome.1@works.pec.it", res.getAddress());
        assertEquals(DigitalDomicileDto.DomicileTypeEnum.PEC, res.getDomicileType());
    }


    @Test
    void getOneDigitalDomicileNotFound() {
        //GIVEN
        UUID id = UUID.fromString("d0a235b2-a454-11ec-b909-0242ac120002");


        // WHEN
        assertThrows(NotFoundException.class, () -> service.getOneDigitalDomicile(RecipientTypeDto.PF, id));
        //THEN
    }
}