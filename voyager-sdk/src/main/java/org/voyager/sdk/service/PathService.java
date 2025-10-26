package org.voyager.sdk.service;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.AirlinePathQuery;
import org.voyager.sdk.model.RoutePathQuery;
import org.voyager.commons.model.route.RoutePath;
import org.voyager.commons.model.route.AirlinePath;
import org.voyager.commons.model.route.PathResponse;
import java.util.List;

public interface PathService {
    Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(AirlinePathQuery airlinePathQuery);
    Either<ServiceError, List<RoutePath>> getRoutePathList(RoutePathQuery routePathQuery);
}
