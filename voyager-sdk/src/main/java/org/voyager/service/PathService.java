package org.voyager.service;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.model.AirlinePathQuery;
import org.voyager.model.RoutePathQuery;
import org.voyager.model.route.RoutePath;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.PathResponse;
import java.util.List;

public interface PathService {
    Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(AirlinePathQuery airlinePathQuery);
    Either<ServiceError, List<RoutePath>> getRoutePathList(RoutePathQuery routePathQuery);
}
