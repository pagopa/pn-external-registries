package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgGroupStatusDto;
import org.springframework.stereotype.Component;

public class UserGroupToPgGroupDtoMapper {

    private UserGroupToPgGroupDtoMapper() {

    }

    public static PgGroupDto toDto(UserGroupResourceDto resource) {
        PgGroupDto dto = new PgGroupDto();
        dto.setId(resource.getId());
        dto.setDescription(resource.getDescription());
        dto.setName(resource.getName());
        dto.setStatus(PgGroupStatusDto.fromValue(resource.getStatus().getValue()));
        return dto;
    }
}
