package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;

public class InstitutionResourceDtoToPaInfoDto {

    private InstitutionResourceDtoToPaInfoDto() {}

    public static PaInfoDto toDto(InstitutionResourceDto entity) {
        PaInfoDto dto = new PaInfoDto();
        dto.setId(entity.getId());
        dto.setTaxId(entity.getFiscalCode());
        dto.setName(entity.getName());
        PaContactsDto gePaContactsDto = new PaContactsDto();
        gePaContactsDto.setPec(entity.getMailAddress());
        dto.setGeneralContacts(gePaContactsDto);
        return  dto;
    }
}
