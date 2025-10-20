package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.AirlinePathQuery;
import org.voyager.model.RoutePathQuery;
import org.voyager.model.route.RoutePath;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.PathResponse;
import org.voyager.service.PathService;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;
import java.util.List;

public class PathServiceImpl implements PathService {
    private final ServiceUtils serviceUtils;

    protected PathServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

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
