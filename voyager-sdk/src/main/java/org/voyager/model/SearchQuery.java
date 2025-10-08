package org.voyager.model;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.utils.Constants;

import java.util.StringJoiner;

public class SearchQuery {
    private String query;
    private Integer skipRowCount;
    private Integer limit;

    SearchQuery(@NotBlank String query, Integer skipRowCount, Integer limit) {
        this.query = query;
        this.skipRowCount = skipRowCount;
        this.limit = limit;
    }

    public static SearchQueryBuilder builder() {
        return new SearchQueryBuilder();
    }

    public static String resolveRequestURL(@NonNull SearchQuery searchQuery) {
        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.QUERY_PARAM_NAME,searchQuery.query));

        Integer skipRowCount = searchQuery.skipRowCount;
        if (skipRowCount != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.SKIP_ROW_PARAM_NAME,skipRowCount));
        }

        Integer limit = searchQuery.limit;
        if (limit != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));
        }
        return String.format("%s?%s", Constants.Voyager.Path.SEARCH_PATH,paramsJoiner);
    }

    public static class SearchQueryBuilder {
        private String query;
        private Integer skipRowCount;
        private Integer limit;

        public SearchQueryBuilder withQuery(@NotBlank String query) {
            this.query = query;
            return this;
        }

        public SearchQueryBuilder withSkipRowCount(@NonNull Integer skipRowCount) {
            this.skipRowCount = skipRowCount;
            return this;
        }

        public SearchQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public SearchQuery build() {
            return new SearchQuery(query,skipRowCount,limit);
        }
    }
}
