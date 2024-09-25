package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDetailDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PgUserDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.UserProductResourceDtoDto;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.util.Objects;

public class UserDataToPgUserDto {

    private UserDataToPgUserDto() {
    }

    public static PgUserDto toDto(UserInstitutionResourceDto resourceDto) {
        PgUserDto dto = new PgUserDto();
        dto.setInstitutionId(resourceDto.getInstitutionId());
        dto.setUserId(resourceDto.getUserId());
        dto.setInstitutionDescription(resourceDto.getInstitutionDescription());
        dto.setInstitutionRootName(resourceDto.getInstitutionRootName());
        dto.setId(resourceDto.getId());
        if(!CollectionUtils.isEmpty(resourceDto.getProducts())){
            dto.setProduct(toUserProductResourceDtoDto(resourceDto.getProducts().get(0)));
        }
        return dto;
    }

    public static PgUserDetailDto toDto(UserResponseDto userResponseDto){
        PgUserDetailDto pgUserDetailDto = new PgUserDetailDto();
        pgUserDetailDto.setId(userResponseDto.getId());
        pgUserDetailDto.setName(userResponseDto.getName());
        pgUserDetailDto.setSurname(userResponseDto.getSurname());
        return pgUserDetailDto;
    }



    private static UserProductResourceDtoDto toUserProductResourceDtoDto(UserProductResourceDto product) {
        UserProductResourceDtoDto dto = new UserProductResourceDtoDto();
        dto.setProductId(product.getProductId());
        if(Objects.nonNull(product.getCreatedAt())){
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            dto.setCreatedAt(dateFormat.format(product.getCreatedAt()));
        }
        if(Objects.nonNull(product.getUpdatedAt())){
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            dto.setUpdatedAt(dateFormat.format(product.getUpdatedAt()));
        }
        dto.setProductRole(product.getProductRole());
        dto.setRole(product.getRole());
        if(Objects.nonNull(product.getStatus())) {
            dto.setStatus(product.getStatus().name());
        }
        return dto;
    }


}
