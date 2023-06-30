package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;

import java.util.Date;

public class OnboardInstitutionEntityToPaInfoDto {

    private OnboardInstitutionEntityToPaInfoDto() {}

    public static PaInfoDto toDto(OnboardInstitutionEntity entity) {
        PaInfoDto dto = new PaInfoDto();
        dto.setId(entity.getInstitutionId());
        dto.setTaxId(entity.getTaxCode());
        dto.setName(entity.getDescription());
        dto.setIpaCode(entity.getIpaCode());
        dto.setSdiCode(entity.getSdiCode());
        dto.setAgreementDate(entity.getCreated() != null ? Date.from(entity.getCreated()) : null);
        PaContactsDto gePaContactsDto = new PaContactsDto();
        gePaContactsDto.setPec(entity.getDigitalAddress());
        gePaContactsDto.setRegisteredOffice(entity.getAddress());
        dto.setGeneralContacts(gePaContactsDto);
        return  dto;
    }
}
