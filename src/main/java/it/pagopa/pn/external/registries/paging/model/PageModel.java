package it.pagopa.pn.external.registries.paging.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;


public class PageModel<T> extends PageImpl<T> {

    /**
     * Creates a new instance of {@link PageModel} by applying pagination logic on the provided list.
     * This method ensures that only the required subset of elements is included in the returned paginated model.
     *
     * @param content The complete list of elements to paginate.
     * @param pageable The {@link Pageable} object containing pagination information (page number, size, etc.).
     * @param <A> The type of elements contained in the list.
     * @return A {@link PageModel} instance containing only the elements relevant to the requested page.
     */
    public static <A> PageModel<A> builder(List<A> content, Pageable pageable){
        int first = (int) Math.min(pageable.getOffset(), content.size());
        int last = Math.min(first + pageable.getPageSize(), content.size());
        int size = content.size();
        return new PageModel<>(content.subList(first, last), pageable, size);
    }

    private PageModel(List<T> content, Pageable pageable, long size) {
        super(content, pageable, size);
    }
}