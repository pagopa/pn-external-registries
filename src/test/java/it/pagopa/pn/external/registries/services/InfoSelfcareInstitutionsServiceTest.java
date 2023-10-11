package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.exceptions.PnRootIdNotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.RootSenderIdResponseDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.services.helpers.OnboardInstitutionFulltextSearchHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(LocalStackTestConfig.class)
@Slf4j
@ActiveProfiles("test")
class InfoSelfcareInstitutionsServiceTest {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private InfoSelfcareInstitutionsService service;

    @MockBean
    private OnboardInstitutionsDao onboardInstitutionsDao;

    @MockBean
    private OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper;

    @Test
    void getOnePa() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        OnboardInstitutionEntity inst = new OnboardInstitutionEntity();
        inst.setPk(UUID.fromString(id).toString());
        inst.setDescription("Comune di Milano");
        inst.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        inst.setTaxCode("123456789");

        Mockito.when(onboardInstitutionsDao.get(Mockito.anyString())).thenReturn(Mono.just(inst));

        // WHEN
        PaInfoDto res = service.getOnePa(id).block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.getName());
        assertEquals(id, res.getId());
    }

    @Test
    void getOnePaNotExist() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        Mockito.when(onboardInstitutionsDao.get(Mockito.anyString())).thenReturn(Mono.empty());

        // WHEN
        Mono<PaInfoDto> mono =service.getOnePa(id);
        assertThrows(PnPANotFoundException.class, () -> {
            mono.block(d);
        });
        //THEN
    }

    @Test
    void listOnboardedPaByName() {
        //GIVEN
        String name = "comune";
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        List<PaSummaryDto> list = new ArrayList<>();
        PaSummaryDto inst = new PaSummaryDto();
        inst.setId(UUID.fromString(id).toString());
        inst.setName("Comune di Milano");

        list.add(inst);
        inst = new PaSummaryDto();
        inst.setId(UUID.randomUUID().toString());
        inst.setName("Comune di Verona");
        list.add(inst);


        Mockito.when(onboardInstitutionFulltextSearchHelper.fullTextSearch(Mockito.anyString())).thenReturn(Flux.fromIterable(list));


        // WHEN
        List<PaSummaryDto> res = service.listOnboardedPaByName(name).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.get(0).getName());
    }

    @Test
    void listOnboardedPaByIds() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        String id1 = "d0d28367-1695-4c50-a260-6fda526e9aac";
        List<OnboardInstitutionEntity> list = new ArrayList<>();
        OnboardInstitutionEntity inst = new OnboardInstitutionEntity();
        inst.setPk(UUID.fromString(id).toString());
        inst.setDescription("Comune di Milano");
        inst.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        inst.setTaxCode("123456789");

        list.add(inst);

        inst = new OnboardInstitutionEntity();
        inst.setPk(UUID.fromString(id1).toString());
        inst.setDescription("Comune di Verona");
        inst.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        inst.setTaxCode("123456799");

        list.add(inst);

        Mockito.when(onboardInstitutionsDao.get(list.get(0).getPk())).thenReturn(Mono.just(list.get(0)));
        Mockito.when(onboardInstitutionsDao.get(list.get(1).getPk())).thenReturn(Mono.just(list.get(1)));

        // WHEN
        List<PaSummaryDto> res = service.listOnboardedPaByIds(List.of(id,id1)).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("Comune di Milano", res.get(0).getName());
        assertEquals("Comune di Verona", res.get(1).getName());
    }


    @Test
    void getRootId() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        String rootId = "d0d28368-1695-0f00-a260-6fda526e9aab";
        OnboardInstitutionEntity inst = new OnboardInstitutionEntity();
        inst.setPk(UUID.fromString(id).toString());
        inst.setRootId(rootId);
        inst.setDescription("Comune di Milano");
        inst.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        inst.setTaxCode("123456789");

        Mockito.when(onboardInstitutionsDao.get(Mockito.anyString())).thenReturn(Mono.just(inst));

        // WHEN
        RootSenderIdResponseDto rootSenderIdResponseDto = service.getRootId(id).block();


        //THEN
        assertNotNull(rootSenderIdResponseDto);
        assertEquals(rootId, rootSenderIdResponseDto.getRootId());
        assertNotEquals(id, rootSenderIdResponseDto.getRootId());
    }


    @Test
    void getRootIdSenderNull() {
        //GIVEN
        String id = "d0d28368-1695-0f00-a260-6fda526e9aab";
        Mockito.when(onboardInstitutionsDao.get(Mockito.anyString())).thenReturn(Mono.empty());

        // WHEN
        Mono<RootSenderIdResponseDto> monoResponse = service.getRootId(id);

        assertThrows(PnRootIdNotFoundException.class, () -> {
            monoResponse.block(d);
        });
    }

    @Test
    void filterOutRootIds() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        String rootId = "d0d28368-1695-0f00-a260-6fda526e9aab";
        OnboardInstitutionEntity paNotRoot = new OnboardInstitutionEntity();
        paNotRoot.setPk(UUID.fromString(id).toString());
        paNotRoot.setRootId(rootId);
        paNotRoot.setDescription("Comune di Milano");
        paNotRoot.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        paNotRoot.setTaxCode("123456789");


        String secondId = "f1f12345-1695-4c50-a260-6fda526e9aab";
        String secondRootId = "f1f12345-1695-4c50-a260-6fda526e9aab";
        OnboardInstitutionEntity paRoot = new OnboardInstitutionEntity();
        paRoot.setPk(UUID.fromString(secondId).toString());
        paRoot.setRootId(secondRootId);
        paRoot.setDescription("Comune di Roma");
        paRoot.setStatus(OnboardInstitutionEntity.STATUS_ACTIVE);
        paRoot.setTaxCode("123456789");

        Mockito.when(onboardInstitutionsDao.filterOutRootIds(Mockito.anyList())).thenReturn(Flux.just(paNotRoot));

        // WHEN
        Flux<String> stringFlux = service.filterOutRootIds(List.of(paRoot.getInstitutionId(), paNotRoot.getInstitutionId()));
        Mono<List<String>> risultatiListMono = stringFlux.collectList();
        List<String> risultatiList = risultatiListMono.block();


        //THEN
        assertNotNull(risultatiList);
        assertFalse(risultatiList.isEmpty());
        assertTrue(risultatiList.size() == 1);
        assertEquals(risultatiList.get(0), paNotRoot.getInstitutionId());
    }

}