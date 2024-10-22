package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.mapper.LanguageDetailEntityToAdditionalLanguagesDtoMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_PACONFIGNOTFOUND;

@Slf4j
@Service
@AllArgsConstructor
public class InfoLanguageService {

    public static final String ADDITIONAL_LANG_NOTFOUND = "Non è stata trovata nessuna lingua aggiuntiva configurata per la PA";
    private final SenderConfigurationDao senderConfigurationDao;

    public Mono<AdditionalLanguagesDto> retrievePaAdditionalLang(String paId) {
        log.info("start retrieving additional languages for PA: [{}]", paId);
        return senderConfigurationDao.getSenderConfiguration(paId, SenderConfigurationType.LANG)
                .switchIfEmpty(Mono.error(new PnInternalException(ADDITIONAL_LANG_NOTFOUND, 404, ERROR_CODE_EXTERNALREGISTRIES_PACONFIGNOTFOUND)))
                .map(LanguageDetailEntityToAdditionalLanguagesDtoMapper::toDto);
    }

}
