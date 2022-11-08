package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstitutionResourceDtoToPaInfoDtoTest {

    @Test
    void toDto() {
        //GIVEN
        InstitutionResourceDto institutionResourceDto = new InstitutionResourceDto();
        institutionResourceDto.setId(UUID.fromString("c0a235b2-a454-11ec-b909-0242ac120002"));
        institutionResourceDto.setTaxCode("mocked-taxCode");
        institutionResourceDto.setDescription("mocked-description");
        institutionResourceDto.setDigitalAddress("mocked-digital-address");

        // WHEN
        PaInfoDto res = InstitutionResourceDtoToPaInfoDto.toDto(institutionResourceDto);

        //THEN
        assertNotNull(res);
        assertEquals(res.getId(),institutionResourceDto.getId().toString());
        assertEquals(res.getName(),institutionResourceDto.getDescription());
        assertEquals(res.getTaxId(),institutionResourceDto.getTaxCode());
        assertEquals(res.getGeneralContacts().getPec(), institutionResourceDto.getDigitalAddress());
    }
}
