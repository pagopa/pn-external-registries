package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;

public class OnboardInstitutionEntityToPaInfoDto {

    private OnboardInstitutionEntityToPaInfoDto() {}

    public static PaInfoDto toDto(OnboardInstitutionEntity entity) {
        PaInfoDto dto = new PaInfoDto();
        dto.setId(entity.getInstitutionId());
        dto.setTaxId(entity.getTaxCode());
        dto.setName(entity.getDescription());
        PaContactsDto gePaContactsDto = new PaContactsDto();
        gePaContactsDto.setPec(entity.getDigitalAddress());
        dto.setGeneralContacts(gePaContactsDto);
        return  dto;
    }
}
