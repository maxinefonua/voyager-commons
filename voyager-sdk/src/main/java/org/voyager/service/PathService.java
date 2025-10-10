package org.voyager.service;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.model.PathAirlineQuery;
import org.voyager.model.PathQuery;
import org.voyager.model.route.RoutePath;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.PathResponse;
import java.util.List;

public interface PathService {
    Either<ServiceError, PathResponse<AirlinePath>> getAirlinePathResponse(PathAirlineQuery pathAirlineQuery);
    Either<ServiceError, List<RoutePath>> getRoutePathList(PathQuery pathQuery);
}
