package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.SearchQuery;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public interface SearchService {
    Either<ServiceError, SearchResult<ResultSearch>> search(SearchQuery searchQuery);
    Either<ServiceError, LookupAttribution> attribution();
    Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId);
}
