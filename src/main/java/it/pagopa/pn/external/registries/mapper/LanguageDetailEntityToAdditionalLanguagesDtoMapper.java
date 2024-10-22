package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LanguageDetailEntityToAdditionalLanguagesDtoMapper {

    public static AdditionalLanguagesDto toDto(LanguageDetailEntity entity) {
        AdditionalLanguagesDto dto = new AdditionalLanguagesDto();
        dto.setPaId(LanguageDetailEntity.getPaId(entity.getHashKey()));
        dto.setAdditionalLanguages(entity.getValue().getAdditionalLangs());
        return dto;
    }
}
