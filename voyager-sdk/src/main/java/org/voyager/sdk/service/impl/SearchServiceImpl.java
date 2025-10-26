package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.SearchQuery;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.commons.model.result.ResultSearchFull;
import org.voyager.sdk.service.SearchService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;

public class SearchServiceImpl implements SearchService {
    private final ServiceUtils serviceUtils;

    SearchServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    @SuppressWarnings("unused")
    SearchServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, SearchResult<ResultSearch>> search(SearchQuery searchQuery) {
        return serviceUtils.fetch(searchQuery.getRequestURL(), HttpMethod.GET,
                new TypeReference<>() {
                });
    }

    @Override
    public Either<ServiceError, LookupAttribution> attribution() {
        return serviceUtils.fetch(Path.Admin.ATTRIBUTION, HttpMethod.GET,LookupAttribution.class);
    }

    @Override
    public Either<ServiceError, ResultSearchFull> fetchResultSearchFull(String sourceId) {
        String requestURL = String.format("%s/%s",Path.Admin.FETCH,sourceId);
        return serviceUtils.fetch(requestURL, HttpMethod.GET,ResultSearchFull.class);
    }
}
