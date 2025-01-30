package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.paging.adapter.PageableDtoAdapter;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedResponseDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.paging.model.PageModel;
import org.springframework.data.domain.Pageable;
import java.util.*;


public class OnboardInstitutionEntityToPaSummaryExtendedDtoMapper {
    private OnboardInstitutionEntityToPaSummaryExtendedDtoMapper() {}

    /**
     * Converte un'istanza di {@link OnboardInstitutionEntity} in un oggetto di tipo {@link PaSummaryExtendedDto}.
     * Questo metodo estrae l'ID e la descrizione dell'entità e li mappa in un nuovo DTO.
     *
     * @param entity l'istanza di {@link OnboardInstitutionEntity} da convertire.
     * @return un oggetto {@link PaSummaryExtendedDto} contenente i dati estratti dall'entità.
     */
    public static PaSummaryExtendedDto toDto(OnboardInstitutionEntity entity) {
        PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
        dto.setId(entity.getInstitutionId());
        dto.setName(entity.getDescription());
        dto.setExtendedList(new ArrayList<>());
        return  dto;
    }

    /**
     * Converte un {@link PageModel} di {@link PaSummaryExtendedDto} in un oggetto paginato
     * {@link PaSummaryExtendedResponseDto}.
     *
     * @param pageModel Il modello di paginazione contenente gli elementi da restituire.
     * @return Un oggetto {@link PaSummaryExtendedResponseDto} con il formato corretto.
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
            summaryDto.setExtendedList(pageModel.getContent().get(0).getExtendedList());
            responseDto.setContent(List.of(summaryDto));
        } else {
            responseDto.setContent(pageModel.getContent());
        }

        return responseDto;
    }

    /**
     * Converte una lista di {@link PaSummaryExtendedDto} in un oggetto paginato di tipo {@link PageModel}.
     * Questo metodo permette di gestire la paginazione dei risultati in base ai parametri forniti.
     *
     * @param pageable l'oggetto {@link Pageable} che definisce il numero di elementi per pagina e l'offset.
     * @param list la lista di {@link PaSummaryExtendedDto} da paginare.
     * @return un oggetto {@link PageModel} contenente la porzione paginata della lista.
     */
    public static PageModel<PaSummaryExtendedDto> toPaginationPaSummaryExtended(Pageable pageable, List<PaSummaryExtendedDto> list){
        return PageModel.builder(list, pageable);
    }
}