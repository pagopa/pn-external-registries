package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstitutionResourceDtoToPaInfoDtoTest {

    @Test
    void toDto() {
        //GIVEN
        OnboardInstitutionEntity institutionResourceDto = new OnboardInstitutionEntity();
        institutionResourceDto.setPk(UUID.fromString("c0a235b2-a454-11ec-b909-0242ac120002").toString());
        institutionResourceDto.setTaxCode("mocked-taxCode");
        institutionResourceDto.setDescription("mocked-description");
        institutionResourceDto.setDigitalAddress("mocked-digital-address");

        // WHEN
        PaInfoDto res = OnboardInstitutionEntityToPaInfoDto.toDto(institutionResourceDto);

        //THEN
        assertNotNull(res);
        assertEquals(res.getId(),institutionResourceDto.getInstitutionId());
        assertEquals(res.getName(),institutionResourceDto.getDescription());
        assertEquals(res.getTaxId(),institutionResourceDto.getTaxCode());
        assertEquals(res.getGeneralContacts().getPec(), institutionResourceDto.getDigitalAddress());
    }
}
