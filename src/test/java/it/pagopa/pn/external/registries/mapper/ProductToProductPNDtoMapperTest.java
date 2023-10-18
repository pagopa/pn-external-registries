package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductRoleDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductRoleInfoResDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.ProductResourcePNDto;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductToProductPNDtoMapperTest {

    @Test
    void toDto() {
        // Given
        ProductResourceDto productResourceDto = new ProductResourceDto();
        productResourceDto.setCreatedAt(new Date());
        productResourceDto.setDescription("Comune di Milano");
        productResourceDto.setId("test1-pn");
        productResourceDto.setLogo("http://test.com/logo.csv");
        productResourceDto.setLogoBgColor("#0066CC");
        productResourceDto.setIdentityTokenAudience("identityToken");
        productResourceDto.setTitle("SEND - Servizio Notifiche Digitali");
        productResourceDto.setDepictImageUrl("https://hostname/path/of/product/depict-image.jpeg");
        productResourceDto.setIdentityTokenAudience("token.value.notifichedigitali");
        productResourceDto.setParentId("test2-pn");
        productResourceDto.setRoleManagementURL("string");
        productResourceDto.setUrlBO("https://hostname/#value=fake");
        productResourceDto.setUrlPublic("https://hostname/pubbliche-amministrazioni");
        Map<String, ProductRoleInfoResDto> productRoleInfoResDtoMap = new HashMap<>();
        ProductRoleInfoResDto productRoleInfoResDto = new ProductRoleInfoResDto();
        productRoleInfoResDto.setMultiroleAllowed(false);
        List<ProductRoleDto> productRoleDtoList = new ArrayList<>();
        ProductRoleDto productRoleDto = new ProductRoleDto();
        productRoleDto.setCode("test");
        productRoleDto.setDescription("description");
        productRoleDto.setLabel("tester");
        productRoleDtoList.add(productRoleDto);
        productRoleInfoResDto.setRoles(productRoleDtoList);
        productRoleInfoResDtoMap.put("MANAGER", productRoleInfoResDto);
        productResourceDto.setRoleMappings(productRoleInfoResDtoMap);

        // When
        ProductResourcePNDto productResourcePNDto = ProductToProductPNDtoMapper.toDto(productResourceDto);

        // Then
        assertNotNull(productResourcePNDto);
        assertEquals(productResourcePNDto.getId(), productResourceDto.getId());
        assertEquals(productResourcePNDto.getParentId(), productResourceDto.getParentId());
        assertEquals(productResourcePNDto.getLogoBgColor(), productResourceDto.getLogoBgColor());
        assertEquals(productResourcePNDto.getRoleMappings().get("MANAGER").getMultiroleAllowed(), productResourceDto.getRoleMappings().get("MANAGER").getMultiroleAllowed());
    }
}