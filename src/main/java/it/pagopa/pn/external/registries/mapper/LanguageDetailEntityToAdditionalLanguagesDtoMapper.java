package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LanguageDetailEntityToAdditionalLanguagesDtoMapper {
    private static final String CONFIG_PREFIX = "CFG-";

    public static AdditionalLanguagesDto toDto(LanguageDetailEntity entity) {
        AdditionalLanguagesDto dto = new AdditionalLanguagesDto();
        dto.setPaId(entity.getPk().replace(CONFIG_PREFIX, ""));
        dto.setAdditionalLanguages(createListFromMap(entity.getValue()));
        return dto;
    }

    public static List<String> createListFromMap(Map<String, String> map) {
        return new ArrayList<>(map.values());
    }
}
