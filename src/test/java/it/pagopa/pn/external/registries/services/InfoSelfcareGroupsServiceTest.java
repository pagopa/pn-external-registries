package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.PageOfUserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupStatusDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaUserGroupClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgUserGroupClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {InfoSelfcareGroupsService.class})
@ExtendWith(SpringExtension.class)
class InfoSelfcareGroupsServiceTest {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private InfoSelfcareGroupsService service;

    @MockBean
    private SelfcarePaUserGroupClient selfcarePaUserGroupClient;

    @MockBean
    private SelfcarePgUserGroupClient selfcarePgUserGroupClient;

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

        when(selfcarePaUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getPaGroups(id, id, null, null).collectList().block(d);


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
        inst2.setId(id + "2");
        inst2.setName("gruppo2");
        inst2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst2);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(list);

        when(selfcarePaUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getPaGroups(id, id, List.of(id), null).collectList().block(d);

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
        inst2.setId(id + "2");
        inst2.setName("gruppo2");
        inst2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);
        list.add(inst2);

        UserGroupResourceDto inst3 = new UserGroupResourceDto();
        inst3.setId(id + "3");
        inst3.setName("gruppo3");
        inst3.setStatus(UserGroupResourceDto.StatusEnum.SUSPENDED);
        list.add(inst3);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(list);

        when(selfcarePaUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // WHEN
        List<PaGroupDto> res = service.getPaGroups(id, id, List.of(id, id + "3"), PaGroupStatusDto.ACTIVE).collectList().block(d);


        //THEN
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(inst1.getName(), res.get(0).getName());
    }

    @Test
    void getPgGroups() {
        // Given
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        UserGroupResourceDto groupDto = new UserGroupResourceDto();
        groupDto.setId(id);
        groupDto.setName("gruppo1");
        groupDto.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(List.of(groupDto));

        when(selfcarePgUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // When
        List<PgGroupDto> res = service.getPgGroups(id, id, null, null).collectList().block(d);

        // Then
        assertNotNull(res);
        assertEquals(groupDto.getName(), res.get(0).getName());
    }

    @Test
    void testPgGroupsCxIdPrefix() {
        // Given
        String id = "PG-d0d28367-1695-4c50-a260-6fda526e9aab";

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(Collections.emptyList());

        when(selfcarePgUserGroupClient.getUserGroups(id.replace("PG-", ""))).thenReturn(Mono.just(response));

        // When
        List<PgGroupDto> res = service.getPgGroups(id, id, null, null).collectList().block(d);

        // Then
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getPgGroupsFiltered() {
        // Given
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";

        UserGroupResourceDto groupDto1 = new UserGroupResourceDto();
        groupDto1.setId(id);
        groupDto1.setName("gruppo1");
        groupDto1.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        UserGroupResourceDto groupDto2 = new UserGroupResourceDto();
        groupDto2.setId(id + "2");
        groupDto2.setName("gruppo2");
        groupDto2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(List.of(groupDto1, groupDto2));

        when(selfcarePgUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // When
        List<PgGroupDto> res = service.getPgGroups(id, id, List.of(id), null).collectList().block(d);

        // Then
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(groupDto1.getName(), res.get(0).getName());
    }

    @Test
    void getPgGroupsFilteredActive() {
        // Given
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";

        UserGroupResourceDto groupDto1 = new UserGroupResourceDto();
        groupDto1.setId(id);
        groupDto1.setName("gruppo1");
        groupDto1.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        UserGroupResourceDto groupDto2 = new UserGroupResourceDto();
        groupDto2.setId(id + "2");
        groupDto2.setName("gruppo2");
        groupDto2.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        UserGroupResourceDto groupDto3 = new UserGroupResourceDto();
        groupDto3.setId(id + "3");
        groupDto3.setName("gruppo3");
        groupDto3.setStatus(UserGroupResourceDto.StatusEnum.SUSPENDED);

        PageOfUserGroupResourceDto response = new PageOfUserGroupResourceDto();
        response.setContent(List.of(groupDto1, groupDto2, groupDto3));

        when(selfcarePgUserGroupClient.getUserGroups(id)).thenReturn(Mono.just(response));

        // When
        List<PgGroupDto> res = service.getPgGroups(id, id, List.of(id, id + "3"), PgGroupStatusDto.ACTIVE).collectList().block(d);

        // Then
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(groupDto1.getName(), res.get(0).getName());
    }
}
