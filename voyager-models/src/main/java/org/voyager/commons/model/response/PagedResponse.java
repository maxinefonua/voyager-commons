package org.voyager.commons.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data @AllArgsConstructor @NoArgsConstructor
public class PagedResponse<T> {
    private List<T> content;          // Renamed from "results"
    private int page;                 // Current page (0-indexed)
    private int size;                 // Page size (renamed from pageSize)
    private long totalElements;       // Renamed from totalResults
    private int totalPages;
    private boolean last;             // Is this the last page?
    private boolean first;            // Is this the first page? (optional)
    private int numberOfElements;
}
