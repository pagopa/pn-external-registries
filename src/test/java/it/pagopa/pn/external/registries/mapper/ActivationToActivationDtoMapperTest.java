package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActivationToActivationDtoMapperTest {

    @Test
    void toDto() {
        //GIVEN
        Activation activation = new Activation();
        activation.setFiscalCode("mocked-fiscalCode");
        activation.setStatus("ACTIVE");

        // WHEN
        ActivationDto res = ActivationToActivationDtoMapper.toDto(activation);

        //THEN
        assertNotNull(res);
        assertEquals(res.getFiscalCode(),activation.getFiscalCode());
        assertEquals(res.getStatus().getValue(),activation.getStatus());
    }
}
