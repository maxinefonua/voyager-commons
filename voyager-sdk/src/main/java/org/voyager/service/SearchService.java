package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsDefault;
import org.voyager.utils.ServiceUtilsFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.voyager.utils.ConstantsUtils.*;

public class SearchService {
    private static final String SEARCH_PATH = "/search";
    private static final String ATTRIBUTION_PATH = "/search-attribution";
    // TODO: make sub calls /search/fetch, /search/attribution
    private static final String FETCH_PATH = "/fetch";
    private final ServiceUtils serviceUtils;

    SearchService() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    SearchService(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    public Either<ServiceError, LookupAttribution> attribution() {
        return serviceUtils.fetch(ATTRIBUTION_PATH,HttpMethod.GET,LookupAttribution.class);
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = SEARCH_PATH.concat(String.format("?%s=%s", QUERY_PARAM_NAME,query));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }

    public Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId) {
        String requestURL = FETCH_PATH.concat(String.format("/%s",sourceId));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,ResultSearchFull.class);
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query,int limit) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = SEARCH_PATH.concat(String.format("?%s=%s" + "&%s=%s",
                QUERY_PARAM_NAME,query,
                LIMIT_PARAM_NAME,limit));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query,int skipRows,int limit) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = SEARCH_PATH.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s",
                QUERY_PARAM_NAME,query,
                SKIP_ROW_PARAM_NAME,skipRows,
                LIMIT_PARAM_NAME,limit));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }
}
