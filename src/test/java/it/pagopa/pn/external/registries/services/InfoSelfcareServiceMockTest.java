package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.api.v1.mock.InfoPapiImpl;
import it.pagopa.pn.external.registries.exceptions.NotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.client.v1.dto.UserGroupPlainResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InfoSelfcareServiceMockTest {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private InfoSelfcareServiceMock service;

    @MockBean
    private SelfcareClient selfcareClient;

    @MockBean
    private InfoPapiImpl infoPapi;

    @Test
    void getOnePa() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        PaInfoDto inst = new PaInfoDto();
        inst.setId(id);
        inst.setName("Comune di Milano");

        Mockito.when(infoPapi.getOnePa(Mockito.anyString())).thenReturn(Mono.just(inst));

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
        Mockito.when(infoPapi.getOnePa(Mockito.anyString())).thenReturn(Mono.error(new NotFoundException()));


        // WHEN
        Mono<PaInfoDto> mono =service.getOnePa(id);
        assertThrows(NotFoundException.class, () -> mono.block(d));
        //THEN
    }

    @Test
    void listOnboardedPaByName() {
        //GIVEN
        String name = "comune";
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        PaSummaryDto inst = new PaSummaryDto();
        inst.setId(id);
        inst.setName("Comune di Milano");

        List<PaSummaryDto> list = new ArrayList<>();
        list.add(inst);

        Mockito.when(infoPapi.listOnboardedPaByName(name)).thenReturn(Flux.fromIterable(list));


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
        PaSummaryDto inst = new PaSummaryDto();
        inst.setId(id);
        inst.setName("Comune di Milano");

        List<PaSummaryDto> list = new ArrayList<>();
        list.add(inst);

        Mockito.when(infoPapi.listOnboardedPaByIds(List.of(id))).thenReturn(Flux.fromIterable(list));

        // WHEN
        List<PaSummaryDto> res = service.listOnboardedPaByIds(List.of(id)).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.get(0).getName());
    }


    @Test
    void getGroups() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        UserGroupPlainResourceDto inst = new UserGroupPlainResourceDto();
        inst.setId(id);
        inst.setName("gruppo1");
        inst.setStatus(UserGroupPlainResourceDto.StatusEnum.ACTIVE);


        List<UserGroupPlainResourceDto> list = new ArrayList<>();
        list.add(inst);

        Mockito.when(selfcareClient.getUserGroups(id)).thenReturn(Flux.fromIterable(list));

        // WHEN
        List<PaGroupDto> res = service.getGroups(id,id,null, statusFilter).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals(inst.getName(), res.get(0).getName());
    }

    @Test
    void getGroupsFiltered() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        List<UserGroupPlainResourceDto> list = new ArrayList<>();
        UserGroupPlainResourceDto inst1 = new UserGroupPlainResourceDto();
        inst1.setId(id);
        inst1.setName("gruppo1");
        inst1.setStatus(UserGroupPlainResourceDto.StatusEnum.ACTIVE);
        list.add(inst1);

        UserGroupPlainResourceDto inst2 = new UserGroupPlainResourceDto();
        inst2.setId(id+"2");
        inst2.setName("gruppo2");
        inst2.setStatus(UserGroupPlainResourceDto.StatusEnum.ACTIVE);
        list.add(inst2);


        Mockito.when(selfcareClient.getUserGroups(id)).thenReturn(Flux.fromIterable(list));

        // WHEN
        List<PaGroupDto> res = service.getGroups(id,id,List.of(id), statusFilter).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(inst1.getName(), res.get(0).getName());
    }
}