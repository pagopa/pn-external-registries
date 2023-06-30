package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserGroupResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaGroupStatusDto;
import org.springframework.stereotype.Component;

@Component
public class UserGroupToPaGroupDtoMapper {

    private UserGroupToPaGroupDtoMapper(){}

    public static PaGroupDto toDto(UserGroupResourceDto entity) {
        PaGroupDto dto = new PaGroupDto();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());
        dto.setStatus(PaGroupStatusDto.fromValue(entity.getStatus().getValue()));
        return  dto;
    }
}
