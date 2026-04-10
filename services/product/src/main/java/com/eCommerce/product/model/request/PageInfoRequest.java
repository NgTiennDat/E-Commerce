package com.eCommerce.product.model.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public abstract class PageInfoRequest {

    private Integer page = 0;
    private Integer size = 10;

    /** optional */
    private String sortBy;
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    public Pageable toPageable() {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 10 : size;

        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(p, s);
        }

        return PageRequest.of(p, s, Sort.by(sortDirection, sortBy));
    }
}
