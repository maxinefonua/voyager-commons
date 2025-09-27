package org.voyager.model.response;

import lombok.*;

import java.util.List;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter
@ToString
public class SearchResult<T> {
    Integer resultCount;
    List<T> results;
}
