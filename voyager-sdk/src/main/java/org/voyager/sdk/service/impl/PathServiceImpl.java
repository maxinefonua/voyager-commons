package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.AirlinePathQuery;
import org.voyager.sdk.model.RoutePathQuery;
import org.voyager.commons.model.route.RoutePath;
import org.voyager.commons.model.route.AirlinePath;
import org.voyager.commons.model.route.PathResponse;
import org.voyager.sdk.service.PathService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.util.List;

public class PathServiceImpl implements PathService {
    private final ServiceUtils serviceUtils;

    protected PathServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    @SuppressWarnings("unused")
    protected PathServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }


    @Override
    public Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(AirlinePathQuery airlinePathQuery) {
        return serviceUtils.fetch(airlinePathQuery.getRequestURL(),HttpMethod.GET, new TypeReference<>(){});
    }

    @Override
    public Either<ServiceError, List<RoutePath>> getRoutePathList(RoutePathQuery routePathQuery) {
        String requestURL = routePathQuery.getRequestURL();
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<>(){});
    }
}
