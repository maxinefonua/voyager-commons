package org.voyager.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringJoiner;

public class SearchQuery {
    @Getter
    @NotBlank
    private String query;

    @Getter
    @Min(1)
    private Integer skipRowCount;

    @Getter
    @Min(1)
    // TODO: find out max for geonames
    private Integer limit;

    SearchQuery(@NonNull String query, Integer skipRowCount, Integer limit) {
        this.query = query;
        this.skipRowCount = skipRowCount;
        this.limit = limit;
    }

    public static SearchQueryBuilder builder() {
        return new SearchQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.QUERY_PARAM_NAME,URLEncoder.encode(query, StandardCharsets.UTF_8)));

        if (skipRowCount != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.SKIP_ROW_PARAM_NAME,skipRowCount));
        }

        if (limit != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));
        }
        return String.format("%s?%s", Constants.Voyager.Path.SEARCH,paramsJoiner);
    }

    public static class SearchQueryBuilder {
        private String query;
        private Integer skipRowCount;
        private Integer limit;

        public SearchQueryBuilder withQuery(@NonNull String query) {
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
            SearchQuery searchQuery = new SearchQuery(query,skipRowCount,limit);
            JakartaValidationUtil.validate(searchQuery);
            return searchQuery;
        }
    }
}
