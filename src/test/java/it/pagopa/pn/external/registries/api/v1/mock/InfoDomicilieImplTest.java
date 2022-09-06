package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.RecipientTypeDto;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import it.pagopa.pn.external.registries.services.SendPaymentNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InfoDomicilieImplTest {
    private final Duration d = Duration.ofMillis(3000);

    private InfoDomicilieImpl service;

    @MockBean
    private SendPaymentNotificationService paymentNotificationService;

    @MockBean
    private SqsNotificationPaidProducer producer;

    @MockBean
    private MockResponses mrh;

    @BeforeEach
    void setup(){
        this.service = new InfoDomicilieImpl( mrh );
    }

    @Test @Disabled("viene ignorato il mock")
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

    @Test @Disabled("viene ignorato il mock")
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