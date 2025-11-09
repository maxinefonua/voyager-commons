package org.voyager.sdk.service;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.path.airline.PathAirlineQuery;
import org.voyager.sdk.model.RoutePathQuery;
import org.voyager.commons.model.path.route.RoutePath;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.PathResponse;
import java.util.List;

public interface PathService {
    Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(PathAirlineQuery pathAirlineQuery);
    Either<ServiceError, List<RoutePath>> getRoutePathList(RoutePathQuery routePathQuery);
}
