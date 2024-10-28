package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class InfoLanguageServiceTest {

    private InfoLanguageService infoLanguageService;

    @Mock
    private SenderConfigurationDao senderConfigurationDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        infoLanguageService = new InfoLanguageService(senderConfigurationDao);
    }

    @Test
    void testGetAdditionalLang_200_OK() {
        String paId = "testPaId";
        List<String> languages = new ArrayList<>();
        languages.add("DE");
        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey(LanguageDetailEntity.buildPk(paId));
        LangConfig langConfig = new LangConfig();
        langConfig.setAdditionalLangs(languages);
        entity.setValue(langConfig);

        when(senderConfigurationDao.getSenderConfiguration(paId, SenderConfigurationType.LANG))
                .thenReturn(Mono.just(entity));

        AdditionalLanguagesDto response = infoLanguageService.retrievePaAdditionalLang(paId).block();

        assertNotNull(response);
        assertEquals(paId, response.getPaId());
        assertEquals(languages, response.getAdditionalLanguages());
    }

    @Test
    void testGetAdditionalLang_ConfigNotFound() {
        when(senderConfigurationDao.getSenderConfiguration("paId", SenderConfigurationType.LANG))
                .thenReturn(Mono.empty());

        StepVerifier.create(infoLanguageService.retrievePaAdditionalLang("paId"))
                .expectErrorMatches(throwable -> throwable instanceof PnInternalException)
                .verify();

    }

}