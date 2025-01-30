package it.pagopa.pn.external.registries.paging.adapter;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PageableDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.SortDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



public class PageableDtoAdapter {

    /**
     * Converte un oggetto {@link Pageable} di Spring in un oggetto {@link PageableDto}.
     *
     * @param pageable l'oggetto {@link Pageable} di Spring da convertire
     * @return un oggetto {@link PageableDto} che rappresenta la paginazione
     */
    public static PageableDto toPageableDto(Pageable pageable) {
        if (pageable == null) {
            return new PageableDto()
                    .pageNumber(1)
                    .pageSize(1)
                    .offset(0L)
                    .paged(true)
                    .unpaged(false)
                    .sort(toSortDto(Sort.unsorted()));
        }

        return new PageableDto()
                .pageNumber(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .offset(pageable.getOffset())
                .paged(true)
                .unpaged(false)
                .sort(toSortDto(pageable.getSort()));
    }

    /**
     * Converte un oggetto {@link Sort} di Spring in un oggetto {@link SortDto}.
     *
     * @param sort l'oggetto {@link Sort} di Spring da convertire
     * @return un oggetto {@link SortDto} che rappresenta l'ordinamento
     */
    private static SortDto toSortDto(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new SortDto()
                    .unsorted(true)
                    .sorted(false)
                    .empty(true);
        }

        return new SortDto()
                .unsorted(false)
                .sorted(true)
                .empty(false);
    }
}