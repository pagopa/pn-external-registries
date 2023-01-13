package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareInstitutionsClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
@Slf4j
class InfoSelfcareGroupsServiceTest {

    private final Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private InfoSelfcareGroupsService service;

    @Mock
    private SelfcareInstitutionsClient selfcareInstitutionsClient;

    @Mock
    private SelfcareUserGroupClient selfcareUserGroupClient;

    @Test
    void getGroups() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        UserGroupResourceDto inst = new UserGroupResourceDto();
        inst.setId(id);
        inst.setName("gruppo1");
        inst.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);


        List<UserGroupResourceDto> list = new ArrayList<>();
        list.add(inst);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(list);

        Mockito.when(selfcareUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getGroups(id,id,null, null).collectList().block(d);


        //THEN
        assertNotNull(res);
        assertEquals(inst.getName(), res.get(0).getName());
    }

    @Test
    void getGroupsFiltered() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        List<UserGroupResourceDto> list = new ArrayList<>();
        UserGroupResourceDto inst1 = new UserGroupResourceDto();
        inst1.setId(id);
        inst1.setName("gruppo1");
        inst1.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst1);

        UserGroupResourceDto inst2 = new UserGroupResourceDto();
        inst2.setId(id+"2");
        inst2.setName("gruppo2");
        inst2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst2);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(list);

        Mockito.when(selfcareUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getGroups(id,id,List.of(id), null).collectList().block(d);


        //THEN
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(inst1.getName(), res.get(0).getName());
    }


    @Test
    void getGroupsFilteredActive() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        List<UserGroupResourceDto> list = new ArrayList<>();
        UserGroupResourceDto inst1 = new UserGroupResourceDto();
        inst1.setId(id);
        inst1.setName("gruppo1");
        inst1.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst1);

        UserGroupResourceDto inst2 = new UserGroupResourceDto();
        inst2.setId(id+"2");
        inst2.setName("gruppo2");
        inst2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst2);

        UserGroupResourceDto inst3 = new UserGroupResourceDto();
        inst3.setId(id+"3");
        inst3.setName("gruppo3");
        inst3.setStatus(UserGroupResourceDto.StatusEnum.SUSPENDED);
        list.add(inst3);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(list);

        Mockito.when(selfcareUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getGroups(id,id,List.of(id, id+"3"), PaGroupStatusDto.ACTIVE).collectList().block(d);


        //THEN
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(inst1.getName(), res.get(0).getName());
    }
}
