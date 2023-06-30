package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserGroupToPgGroupDtoMapperTest {

    @Test
    void toDto() {
        // Given
        UserGroupResourceDto userGroupResourceDto = new UserGroupResourceDto();
        userGroupResourceDto.setId("id");
        userGroupResourceDto.setName("name");
        userGroupResourceDto.setDescription("description");
        userGroupResourceDto.setStatus(UserGroupResourceDto.StatusEnum.ACTIVE);

        // When
        PgGroupDto pgGroupDto = UserGroupToPgGroupDtoMapper.toDto(userGroupResourceDto);

        // Then
        assertNotNull(pgGroupDto);
        assertEquals(userGroupResourceDto.getId(), pgGroupDto.getId());
        assertEquals(userGroupResourceDto.getName(), pgGroupDto.getName());
        assertEquals(userGroupResourceDto.getDescription(), pgGroupDto.getDescription());
        assertEquals(userGroupResourceDto.getStatus().getValue(), pgGroupDto.getStatus().getValue());
    }
}