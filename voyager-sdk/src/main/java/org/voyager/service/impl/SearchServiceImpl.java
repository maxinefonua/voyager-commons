package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.SearchQuery;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.SearchService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

public class SearchServiceImpl implements SearchService {
    private final ServiceUtils serviceUtils;

    SearchServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    SearchServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, SearchResult<ResultSearch>> search(SearchQuery searchQuery) {
        return serviceUtils.fetch(searchQuery.getRequestURL(), HttpMethod.GET,new TypeReference<SearchResult<ResultSearch>>(){});
    }

    @Override
    public Either<ServiceError, LookupAttribution> attribution() {
        return serviceUtils.fetch(Constants.Voyager.Path.ATTRIBUTION, HttpMethod.GET,LookupAttribution.class);
    }

    @Override
    public Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId) {
        String requestURL = String.format("%s/%s",Constants.Voyager.Path.SEARCH,sourceId);
        return serviceUtils.fetch(requestURL, HttpMethod.GET,ResultSearchFull.class);
    }
}
