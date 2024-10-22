package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.mapper.LanguageDetailEntityToAdditionalLanguagesDtoMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND;

@Slf4j
@Service
@AllArgsConstructor
public class InfoLanguageService {

    private final SenderConfigurationDao senderConfigurationDao;

    public Mono<AdditionalLanguagesDto> get(String paId) {
        if (!StringUtils.hasText(paId)) {
            log.error("getlanguage - paId is empty");
            return Mono.error(new IllegalArgumentException("paId is empty"));
        }
        log.info("getlanguage - paId={}", paId);
        return senderConfigurationDao.getSenderConfiguration(paId)
                .switchIfEmpty(Mono.error(new PnPANotFoundException(ERROR_CODE_EXTERNALREGISTRIES_PANOTFOUND)))
                .map(LanguageDetailEntityToAdditionalLanguagesDtoMapper::toDto);
    }

    public Mono<AdditionalLanguagesDto> createOrUpdateLang(AdditionalLanguagesDto additionalLanguagesDto) {
        log.info("updateLang - lang={} - paId{}", additionalLanguagesDto.getAdditionalLanguages(), additionalLanguagesDto.getPaId());
        return senderConfigurationDao.createOrUpdateLang(additionalLanguagesDto.getPaId(), additionalLanguagesDto.getAdditionalLanguages())
                .map(LanguageDetailEntityToAdditionalLanguagesDtoMapper::toDto);
    }

}
