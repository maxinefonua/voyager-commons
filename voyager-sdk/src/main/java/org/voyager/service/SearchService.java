package org.voyager.service;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.model.SearchQuery;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;

public interface SearchService {
    Either<ServiceError, SearchResult<ResultSearch>> search(SearchQuery searchQuery);
    Either<ServiceError, LookupAttribution> attribution();
    Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId);
}
