package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.AllowedAdditionalLanguages;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.exceptions.AdditionalLangException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.mapper.LanguageDetailEntityToAdditionalLanguagesDtoMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.*;
import static it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity.buildPk;

@Slf4j
@Service
@AllArgsConstructor
public class InfoLanguageService {

    public static final String ADDITIONAL_LANG_NOTFOUND = "Non è stata trovata nessuna lingua aggiuntiva configurata per la PA";
    public static final String REQUIRED_ADDITIONAL_LANG = "La lingua aggiuntiva è obbligatoria";
    private final SenderConfigurationDao senderConfigurationDao;

    public Mono<AdditionalLanguagesDto> retrievePaAdditionalLang(String paId) {
        log.info("start retrieving additional languages for PA: [{}]", paId);
        return senderConfigurationDao.getSenderConfiguration(buildPk(paId), SenderConfigurationType.LANG)
                .switchIfEmpty(Mono.error(new AdditionalLangException(ADDITIONAL_LANG_NOTFOUND, 404, ERROR_CODE_EXTERNALREGISTRIES_PACONFIGNOTFOUND)))
                .map(LanguageDetailEntityToAdditionalLanguagesDtoMapper::toDto)
                .onErrorReturn(AdditionalLangException.class, LanguageDetailEntityToAdditionalLanguagesDtoMapper.toEmptyDto(paId));
    }

    public Mono<AdditionalLanguagesDto> createOrUpdateLang(AdditionalLanguagesDto additionalLanguagesDto) {
        log.info("start putting additional lang for PA: [{}]", additionalLanguagesDto.getPaId());
        if(CollectionUtils.isNullOrEmpty(additionalLanguagesDto.getAdditionalLanguages())){
            return Mono.error(new AdditionalLangException(REQUIRED_ADDITIONAL_LANG, 400, ERROR_CODE_EXTERNALREGISTRIES_REQUIRED_ADDITIONAL_LANGS));
        }
        if(Arrays.stream(AllowedAdditionalLanguages.values()).map(AllowedAdditionalLanguages::name).noneMatch(additionalLanguagesDto.getAdditionalLanguages()::contains)){
            return Mono.error(new AdditionalLangException(String.format("Lingua aggiuntiva non valida, i valori accettati sono %s",
                    Arrays.stream(AllowedAdditionalLanguages.values()).map(Enum::name).collect(Collectors.joining(","))), 400, ERROR_CODE_EXTERNALREGISTRIES_INVALID_ADDITIONAL_LANGS));
        }
        return senderConfigurationDao.createOrUpdateLang(buildPk(additionalLanguagesDto.getPaId()), SenderConfigurationType.LANG, additionalLanguagesDto.getAdditionalLanguages())
                .map(languageDetailEntity -> additionalLanguagesDto);
    }

}
