package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserProductResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class InstitutionsToInstitutionPNDtoMapper {

    private InstitutionsToInstitutionPNDtoMapper() {

    }

    public static InstitutionResourcePNDto toDto(UserInstitutionResourceDto resource) {
        InstitutionResourcePNDto dto = new InstitutionResourcePNDto();
        dto.setDescription(resource.getInstitutionDescription());
        if (StringUtils.hasText(resource.getInstitutionId())) {
            dto.setId(UUID.fromString(resource.getInstitutionId()));
        }
        if (!CollectionUtils.isEmpty(resource.getProducts())) {
            dto.setStatus(retrieveMaxStatus(resource.getProducts()));
            dto.setUserProductRoles(retrieveListOfRole(resource.getProducts()));
        }
        if (StringUtils.hasText(resource.getInstitutionRootName())) {
            RootParentResourcePNDto rootParentResourcePNDto = new RootParentResourcePNDto();
            rootParentResourcePNDto.setDescription(resource.getInstitutionRootName());
            dto.setRootParent(rootParentResourcePNDto);
        }
        return dto;
    }

    private static List<String> retrieveListOfRole(List<UserProductResourceDto> products) {
        return products.stream()
                .map(UserProductResourceDto::getProductRole)
                .filter(StringUtils::hasText)
                .toList();
    }

    private static String retrieveMaxStatus(List<UserProductResourceDto> products) {
        List<UserProductResourceDto.StatusEnum> status = products.stream()
                .map(UserProductResourceDto::getStatus)
                .filter(Objects::nonNull)
                .toList();

        return Collections.min(status).name();
    }

    public static InstitutionResourcePNDto toDto(InstitutionResourceDto resource) {
        InstitutionResourcePNDto dto = new InstitutionResourcePNDto();
        dto.setAddress(resource.getAddress());
        dto.setAooParentCode(resource.getAooParentCode());
        dto.setDescription(resource.getDescription());
        dto.setDigitalAddress(resource.getDigitalAddress());
        dto.setExternalId(resource.getExternalId());
        dto.setId(resource.getId());
        if(resource.getInstitutionType() != null){
            dto.institutionType(InstitutionResourcePNDto.InstitutionTypeEnum.fromValue(resource.getInstitutionType().getValue()));
        }
        dto.setOrigin(resource.getOrigin());
        dto.setOriginId(resource.getOriginId());
        dto.setRecipientCode(resource.getRecipientCode());
        dto.setStatus(resource.getStatus());
        dto.setSubunitCode(resource.getSubunitCode());
        dto.setSubunitType(resource.getSubunitType());
        dto.setTaxCode(resource.getTaxCode());
        dto.setZipCode(resource.getZipCode());
        dto.setUserProductRoles(resource.getUserProductRoles());
        if(resource.getAssistanceContacts() != null) {
            AssistanceContactsResourcePNDto assistanceContactsResourcePNDto = new AssistanceContactsResourcePNDto();
            assistanceContactsResourcePNDto.setSupportEmail(resource.getAssistanceContacts().getSupportEmail());
            assistanceContactsResourcePNDto.setSupportPhone(resource.getAssistanceContacts().getSupportPhone());
            dto.setAssistanceContacts(assistanceContactsResourcePNDto);
        }
        if(resource.getCompanyInformations() != null) {
            CompanyInformationsResourcePNDto companyInformationsResourcePNDto = new CompanyInformationsResourcePNDto();
            companyInformationsResourcePNDto.setRea(resource.getCompanyInformations().getRea());
            companyInformationsResourcePNDto.setBusinessRegisterPlace(resource.getCompanyInformations().getBusinessRegisterPlace());
            companyInformationsResourcePNDto.setShareCapital(resource.getCompanyInformations().getShareCapital());
            dto.setCompanyInformations(companyInformationsResourcePNDto);
        }
        if(resource.getDpoData() != null) {
            DpoDataResourcePNDto dpoDataResourcePNDto = new DpoDataResourcePNDto();
            dpoDataResourcePNDto.setAddress(resource.getDpoData().getAddress());
            dpoDataResourcePNDto.setEmail(resource.getDpoData().getEmail());
            dpoDataResourcePNDto.setPec(resource.getDpoData().getPec());
            dto.setDpoData(dpoDataResourcePNDto);
        }
        if(resource.getRootParent() != null) {
            RootParentResourcePNDto rootParentResourcePNDto = new RootParentResourcePNDto();
            rootParentResourcePNDto.setDescription(resource.getRootParent().getDescription());
            rootParentResourcePNDto.setId(resource.getRootParent().getId());
            dto.setRootParent(rootParentResourcePNDto);
        }
        if(resource.getPspData() != null) {
            PspDataResourcePNDto pspDataResourcePNDto = new PspDataResourcePNDto();
            pspDataResourcePNDto.setAbiCode(resource.getPspData().getAbiCode());
            pspDataResourcePNDto.setBusinessRegisterNumber(resource.getPspData().getBusinessRegisterNumber());
            pspDataResourcePNDto.setLegalRegisterName(resource.getPspData().getLegalRegisterName());
            pspDataResourcePNDto.setLegalRegisterNumber(resource.getPspData().getLegalRegisterNumber());
            pspDataResourcePNDto.setVatNumberGroup(resource.getPspData().getVatNumberGroup());
            dto.setPspData(pspDataResourcePNDto);
        }
        return dto;
    }
}
