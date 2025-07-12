package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.model.route.Route;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.utils.ConstantsUtils.*;

public class SearchService {
    private final String servicePath;
    private final String attributionPath;
    private final String fetchPath;

    SearchService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getSearchPath();
        this.attributionPath = voyagerConfig.getAttributionPath();
        this.fetchPath = voyagerConfig.getfetchPath();
    }

    public Either<ServiceError, LookupAttribution> attribution() {
        return fetch(attributionPath,HttpMethod.GET,LookupAttribution.class);
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = servicePath.concat(String.format("?%s=%s", QUERY_PARAM_NAME,query));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }

    public Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId) {
        String requestURL = fetchPath.concat(String.format("/%s",sourceId));
        return fetch(requestURL, HttpMethod.GET,ResultSearchFull.class);
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query,int limit) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                QUERY_PARAM_NAME,query,
                LIMIT_PARAM_NAME,limit));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }

    public Either<ServiceError, SearchResult<ResultSearch>> search(String query,int skipRows,int limit) {
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s",
                QUERY_PARAM_NAME,query,
                SKIP_ROW_PARAM_NAME,skipRows,
                LIMIT_PARAM_NAME,limit));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }
}
