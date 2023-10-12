package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductRoleInfoResDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductToProductPNDtoMapper {

    private ProductToProductPNDtoMapper() {

    }

    public static ProductResourcePNDto toDto(ProductResourceDto resource) {
        ProductResourcePNDto dto = new ProductResourcePNDto();
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setDepictImageUrl(resource.getDepictImageUrl());
        dto.setDescription(resource.getDescription());
        dto.setId(resource.getId());
        dto.setIdentityTokenAudience(resource.getIdentityTokenAudience());
        dto.setLogo(resource.getLogo());
        dto.setLogoBgColor(resource.getLogoBgColor());
        dto.setParentId(resource.getParentId());
        dto.setRoleManagementURL(resource.getRoleManagementURL());
        dto.setTitle(resource.getTitle());
        dto.setUrlBO(resource.getUrlBO());
        dto.setUrlPublic(resource.getUrlPublic());
        if(resource.getRoleMappings() != null) {
            Map<String, ProductRoleInfoResPNDto> productRoleInfoResPNDtoMap = new HashMap<>();
            Map<String, ProductRoleInfoResDto> roleMappings = resource.getRoleMappings();
            for (var value : roleMappings.entrySet()) {
                ProductRoleInfoResPNDto productRoleInfoResPNDto = new ProductRoleInfoResPNDto();
                productRoleInfoResPNDto.setMultiroleAllowed(roleMappings.get(value.getKey()).getMultiroleAllowed());
                List<ProductRolePNDto> productRolePNDtoList = new ArrayList<>();
                roleMappings.get(value.getKey()).getRoles().forEach(el -> {
                    ProductRolePNDto productRolePNDto = new ProductRolePNDto();
                    productRolePNDto.setCode(el.getCode());
                    productRolePNDto.setDescription(el.getDescription());
                    productRolePNDto.setLabel(el.getLabel());
                    productRolePNDtoList.add(productRolePNDto);
                });
                productRoleInfoResPNDto.setRoles(productRolePNDtoList);
                productRoleInfoResPNDtoMap.put(value.getKey(), productRoleInfoResPNDto);
            }
            dto.setRoleMappings(productRoleInfoResPNDtoMap);
        }
        return dto;
    }
}
