package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto.StatusEnum.ACTIVE;

class UserDataToPgUserDtoTest {

    @Test
    void toDto(){
        UserInstitutionResourceDto userInstitutionResourceDto = new UserInstitutionResourceDto();
        userInstitutionResourceDto.setUserId("userId");
        userInstitutionResourceDto.setInstitutionId("institutionId");
        UserProductResourceDto userProductResourceDto = new UserProductResourceDto();
        userProductResourceDto.setStatus(ACTIVE);
        userProductResourceDto.setProductId("productId");
        userProductResourceDto.setProductRole("admin");
        userProductResourceDto.setRole("role");
        userInstitutionResourceDto.setProducts(Collections.singletonList(userProductResourceDto));

        Assertions.assertNotNull(UserDataToPgUserDto.toDto(userInstitutionResourceDto));
    }
}
