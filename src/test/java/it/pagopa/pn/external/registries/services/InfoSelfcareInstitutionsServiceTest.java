package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.services.helpers.OnboardInstitutionFulltextSearchHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class InfoSelfcareInstitutionsServiceTest {

    private final Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private InfoSelfcareInstitutionsService service;

    @Mock
    private OnboardInstitutionsDao onboardInstitutionsDao;

    @Mock
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

}