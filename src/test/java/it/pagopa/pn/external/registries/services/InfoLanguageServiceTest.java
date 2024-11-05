package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.exceptions.AdditionalLangException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.middleware.db.dao.SenderConfigurationDao;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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

        when(senderConfigurationDao.getSenderConfiguration("CFG_" + paId, SenderConfigurationType.LANG))
                .thenReturn(Mono.just(entity));

        AdditionalLanguagesDto response = infoLanguageService.retrievePaAdditionalLang(paId).block();

        assertNotNull(response);
        assertEquals(paId, response.getPaId());
        assertEquals(languages, response.getAdditionalLanguages());
    }

    @Test
    void testGetAdditionalLang_ConfigNotFound() {
        AdditionalLanguagesDto additionalLanguagesDto = new AdditionalLanguagesDto();
        additionalLanguagesDto.setPaId("paId");
        additionalLanguagesDto.setAdditionalLanguages(Collections.emptyList());
        when(senderConfigurationDao.getSenderConfiguration("CFG_paId", SenderConfigurationType.LANG))
                .thenReturn(Mono.empty());

        StepVerifier.create(infoLanguageService.retrievePaAdditionalLang("paId"))
                .expectNext(additionalLanguagesDto)
                .verifyComplete();

    }

    @Test
    void testGetAdditionalLang_Exception() {
        when(senderConfigurationDao.getSenderConfiguration("CFG_paId", SenderConfigurationType.LANG))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(infoLanguageService.retrievePaAdditionalLang("paId"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();

    }

    @Test
    void testCreateOrUpdateLang_200_OK() {

        List<String> langsList = new ArrayList<>();
        langsList.add("DE");

        LangConfig languages = new LangConfig();
        languages.setAdditionalLangs(langsList);

        String paId = "testPaId";
        AdditionalLanguagesDto request = new AdditionalLanguagesDto();
        request.setPaId(paId);
        request.setAdditionalLanguages(langsList);


        LanguageDetailEntity entity = new LanguageDetailEntity();
        entity.setHashKey(paId);
        entity.setSortKey(SenderConfigurationType.LANG.name());
        entity.setValue(languages);

        when(senderConfigurationDao.createOrUpdateLang("CFG_"+paId, SenderConfigurationType.LANG, langsList))
                .thenReturn(Mono.just(paId));

        StepVerifier.create(infoLanguageService.createOrUpdateLang(request))
                .expectNext(request)
                .verifyComplete();

        Mockito.verify(senderConfigurationDao, times(0)).deleteSenderConfiguration("CFG_"+paId, SenderConfigurationType.LANG);
        Mockito.verify(senderConfigurationDao, times(1)).createOrUpdateLang("CFG_"+paId, SenderConfigurationType.LANG, langsList);
    }

    @Test
    void testDeleteLang_200_OK() {
        String paId = "testPaId";
        AdditionalLanguagesDto request = new AdditionalLanguagesDto();
        request.setPaId(paId);
        request.setAdditionalLanguages(Collections.emptyList());

        when(senderConfigurationDao.deleteSenderConfiguration("CFG_"+paId, SenderConfigurationType.LANG))
                .thenReturn(Mono.just(paId));

        StepVerifier.create(infoLanguageService.createOrUpdateLang(request))
                .expectNext(request)
                .verifyComplete();

        Mockito.verify(senderConfigurationDao, times(1)).deleteSenderConfiguration("CFG_"+paId, SenderConfigurationType.LANG);
        Mockito.verify(senderConfigurationDao, times(0)).createOrUpdateLang(any(), any(), any());
    }

    @Test
    void testCreateOrUpdateLang_WithInvalidAdditionalLangs() {

        List<String> langsList = new ArrayList<>();
        langsList.add("INVALID");

        String paId = "testPaId";
        AdditionalLanguagesDto request = new AdditionalLanguagesDto();
        request.setPaId(paId);
        request.setAdditionalLanguages(langsList);


        StepVerifier.create(infoLanguageService.createOrUpdateLang(request))
                .expectErrorMatches(throwable -> throwable instanceof AdditionalLangException)
                .verify();
    }


}