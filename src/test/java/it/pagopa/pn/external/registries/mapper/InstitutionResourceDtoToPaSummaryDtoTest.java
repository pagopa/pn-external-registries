package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstitutionResourceDtoToPaSummaryDtoTest {

    @Test
    void toDto() {
        //GIVEN
        InstitutionResourceDto institutionResourceDto = new InstitutionResourceDto();
        institutionResourceDto.setId(UUID.fromString("c0a235b2-a454-11ec-b909-0242ac120002"));
        institutionResourceDto.setDescription("mocked-description");

        // WHEN
        PaSummaryDto res = InstitutionResourceDtoToPaSummaryDto.toDto(institutionResourceDto);

        //THEN
        assertNotNull(res);
        assertEquals(res.getId(),institutionResourceDto.getId().toString());
        assertEquals(res.getName(),institutionResourceDto.getDescription());
    }
}
