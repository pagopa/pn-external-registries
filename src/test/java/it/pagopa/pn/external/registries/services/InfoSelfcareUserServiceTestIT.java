package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(LocalStackTestConfig.class)
@Slf4j
@ActiveProfiles("test")
class InfoSelfcareUserServiceTestIT {


    @Autowired
    private InfoSelfcareUserService service;

    @MockBean
    private SelfcarePaInstitutionClient selfcarePaInstitutionClient;

    @Test
    void listUserInstitutionByCurrentUser() {
        //GIVEN
        String user = "d0d28367-1695-4c50-a260-6fda526e9aab";
        UUID institutionId1 = UUID.randomUUID();
        UUID institutionId2 = UUID.randomUUID();

        List<UserInstitutionResourceDto> list = new ArrayList<>();
        UserInstitutionResourceDto dto = new UserInstitutionResourceDto();
        dto.setInstitutionId(institutionId1.toString());
        List<UserProductResourceDto> userProductRoles = new ArrayList<>();
        UserProductResourceDto productResourceDto = new UserProductResourceDto();
        productResourceDto.setProductRole("admin");
        productResourceDto.setStatus(UserProductResourceDto.StatusEnum.ACTIVE);
        userProductRoles.add(productResourceDto);
        dto.setProducts(userProductRoles);
        list.add(dto);
        dto.setInstitutionId(institutionId2.toString());
        list.add(dto);
        Mockito.when(selfcarePaInstitutionClient.getUserInstitutions(user)).thenReturn(Flux.fromIterable(list));
        List<String> header = new ArrayList<>();
        // WHEN
        List<InstitutionResourcePNDto> res = service.listUserInstitutionByCurrentUser(user, "", "WEB", header, "PA").collectList().block();

        //THEN
        assertNotNull(res);
        assertEquals(2, res.size());
        res.forEach(x -> {
            if (x.getId().equals(institutionId1)) {
                assertEquals(institutionId1, x.getId());
            } else {
                assertEquals(institutionId2, x.getId());
            }
        });
    }


}