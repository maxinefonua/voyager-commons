package org.voyager.sdk.service;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.SearchQuery;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.commons.model.result.ResultSearchFull;

public interface SearchService {
    Either<ServiceError, SearchResult<ResultSearch>> search(SearchQuery searchQuery);
    Either<ServiceError, LookupAttribution> attribution();
    Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId);
}
