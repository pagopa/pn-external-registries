package it.pagopa.pn.external.registries.paging.adapter;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PageableDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.SortDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



public class PageableDtoAdapter {

    /**
     * Converts an object {@link Pageable} of Spring in an object {@link PageableDto}.
     *
     * @param pageable the object {@link Pageable} of Spring to convert
     * @return an object {@link PageableDto} which represents the pagination
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
     * Converts an object {@link Sort} of Spring in an object {@link SortDto}.
     *
     * @param sort the object {@link Sort} of Spring to convert
     * @return an object {@link SortDto} which represents the order
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