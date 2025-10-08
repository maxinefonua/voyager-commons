package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.PathAirlineQuery;
import org.voyager.model.PathQuery;
import org.voyager.model.route.RoutePath;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.PathResponse;
import org.voyager.service.PathService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;
import java.util.StringJoiner;

public class PathServiceImpl implements PathService {
    private final ServiceUtils serviceUtils;

    protected PathServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    protected PathServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }


    @Override
    public Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(PathAirlineQuery pathAirlineQuery) {
        String requestURL = PathAirlineQuery.resolveRequestURL(pathAirlineQuery);
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<PathResponse<AirlinePath>>() {});
    }

    @Override
    public Either<ServiceError, List<RoutePath>> getRoutePathList(PathQuery pathQuery) {
        String requestURL = PathQuery.resolveRequestURL(pathQuery);
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<List<RoutePath>>() {});
    }
}
