package org.voyager.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter
public class SearchResult<T> {
    Integer resultCount;
    List<T> results;
}
