package org.voyager.commons.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter
@ToString
public class SearchResult<T> {
    Integer resultCount;
    List<T> results;
}
