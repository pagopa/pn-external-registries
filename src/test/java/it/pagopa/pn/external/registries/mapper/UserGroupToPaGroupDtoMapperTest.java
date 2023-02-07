package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserGroupToPaGroupDtoMapperTest {
    @Test
    void toDto() {
        //GIVEN
        UserGroupResourceDto userGroupResourceDto = new UserGroupResourceDto();
        userGroupResourceDto.setId("mocked-id");
        userGroupResourceDto.setName("mocked-name");
        userGroupResourceDto.setDescription("mocked-description");
        userGroupResourceDto.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        // WHEN
        PaGroupDto res = UserGroupToPaGroupDtoMapper.toDto(userGroupResourceDto);

        //THEN
        assertNotNull(res);
        assertEquals(res.getId(),userGroupResourceDto.getId());
        assertEquals(res.getName(),userGroupResourceDto.getName());
        assertEquals(res.getDescription(),userGroupResourceDto.getDescription());
        assertEquals(res.getStatus().getValue(),userGroupResourceDto.getStatus().getValue());
    }
}
