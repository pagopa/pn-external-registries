package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.*;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.InstitutionResourcePNDto;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstitutionToInstitutionPNDtoMapperTest {

    @Test
    void toDto() {
        // Given
        UUID uuid = UUID.randomUUID();
        InstitutionResourceDto institutionResourceDto = new InstitutionResourceDto();
        institutionResourceDto.setAddress("Via vittorio veneto, 23");
        institutionResourceDto.setDescription("Comune di Milano");
        institutionResourceDto.setDigitalAddress("xxx@cert.xxx.it");
        institutionResourceDto.setExternalId("00431230123");
        institutionResourceDto.setId(uuid);
        institutionResourceDto.setInstitutionType(InstitutionResourceDto.InstitutionTypeEnum.PA);
        institutionResourceDto.setZipCode("12345");
        institutionResourceDto.setTaxCode("00431230123");
        institutionResourceDto.setStatus("ACTIVE");
        institutionResourceDto.setOriginId("a_a123");
        institutionResourceDto.setOrigin("ABC");
        List<String> userProductRoles = new ArrayList<>();
        userProductRoles.add("admin");
        institutionResourceDto.setUserProductRoles(userProductRoles);
        institutionResourceDto.setRecipientCode("9ABCDE1");
        CompanyInformationsResourceDto companyInformationsResourceDto = new CompanyInformationsResourceDto();
        institutionResourceDto.setCompanyInformations(companyInformationsResourceDto);
        AssistanceContactsResourceDto assistanceContactsResourceDto = new AssistanceContactsResourceDto();
        assistanceContactsResourceDto.setSupportEmail("email@pec.it");
        institutionResourceDto.setAssistanceContacts(assistanceContactsResourceDto);
        RootParentResourceDto rootParentResourceDto = new RootParentResourceDto();
        institutionResourceDto.setRootParent(rootParentResourceDto);
        DpoDataResourceDto dpoDataResourceDto = new DpoDataResourceDto();
        institutionResourceDto.setDpoData(dpoDataResourceDto);

        // When
        InstitutionResourcePNDto institutionResourcePNDto = InstitutionsToInstitutionPNDtoMapper.toDto(institutionResourceDto);

        // Then
        assertNotNull(institutionResourcePNDto);
        assertEquals(institutionResourcePNDto.getId(), institutionResourceDto.getId());
        assertEquals(institutionResourcePNDto.getRecipientCode(), institutionResourceDto.getRecipientCode());
        assertEquals(institutionResourcePNDto.getDigitalAddress(), institutionResourceDto.getDigitalAddress());
        assertEquals(institutionResourcePNDto.getAssistanceContacts().getSupportEmail(), institutionResourceDto.getAssistanceContacts().getSupportEmail());
    }
}