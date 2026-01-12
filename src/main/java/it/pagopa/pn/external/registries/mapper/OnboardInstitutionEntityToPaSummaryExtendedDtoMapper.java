package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedResponseDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.paging.adapter.PageableDtoAdapter;
import it.pagopa.pn.external.registries.paging.model.PageModel;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;


public class OnboardInstitutionEntityToPaSummaryExtendedDtoMapper {
    private OnboardInstitutionEntityToPaSummaryExtendedDtoMapper() {}

    /**
     * Converts an instance of {@link OnboardInstitutionEntity} in an object of type {@link PaSummaryExtendedDto}.
     * This method extracts the entity ID and description and maps them to a new DTO.
     *
     * @param entity the instance of {@link OnboardInstitutionEntity} to convert.
     * @return an object {@link PaSummaryExtendedDto} containing data extracted from the entity.
     */
    public static PaSummaryExtendedDto toDto(OnboardInstitutionEntity entity) {
        PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
        dto.setId(entity.getInstitutionId());
        dto.setName(entity.getDescription());
        dto.setChildrenList(new ArrayList<>());
        return  dto;
    }

    /**
     * Convert a {@link PageModel} of {@link PaSummaryExtendedDto} in a paginated object
     * {@link PaSummaryExtendedResponseDto}.
     *
     * @param pageModel The pagination model containing the elements to be returned.
     * @return An object {@link PaSummaryExtendedResponseDto} with the correct format.
     */
    public static PaSummaryExtendedResponseDto toPageableResponseExtended(PageModel<PaSummaryExtendedDto> pageModel) {
        PaSummaryExtendedResponseDto responseDto = new PaSummaryExtendedResponseDto();
        responseDto.setPageable(PageableDtoAdapter.toPageableDto(pageModel.getPageable()));
        responseDto.setNumber(pageModel.getNumber());
        responseDto.setNumberOfElements(pageModel.getNumberOfElements());
        responseDto.setSize(pageModel.getSize());
        responseDto.setTotalElements(pageModel.getTotalElements());
        responseDto.setTotalPages((long) pageModel.getTotalPages());
        responseDto.setFirst(pageModel.isFirst());
        responseDto.setLast(pageModel.isLast());
        responseDto.setEmpty(pageModel.isEmpty());

        if (pageModel.getContent().size() == 1 && pageModel.getContent().get(0).getId() == null) {
            PaSummaryExtendedDto summaryDto = new PaSummaryExtendedDto();
            summaryDto.setChildrenList(pageModel.getContent().get(0).getChildrenList());
            responseDto.setContent(List.of(summaryDto));
        } else {
            responseDto.setContent(pageModel.getContent());
        }

        return responseDto;
    }

    /**
     * Converts a list of {@link PaSummaryExtendedDto} in a paginated object of type {@link PageModel}.
     * This method allows you to manage the pagination of the results based on the parameters provided.
     *
     * @param pageable the object {@link Pageable} which defines the number of elements per page and the offset.
     * @param list a list of {@link PaSummaryExtendedDto} to paginate.
     * @return an object {@link PageModel} containing the paginated portion of the list.
     */
    public static PageModel<PaSummaryExtendedDto> toPaginationPaSummaryExtended(Pageable pageable, List<PaSummaryExtendedDto> list){
        return PageModel.builder(list, pageable);
    }
}