package org.voyager.sdk.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.route.RouteQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import java.util.List;

public interface RouteService {
    Either<ServiceError, List<Route>> getRoutes();
    Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery);
    Either<ServiceError,Route> getRoute(@NonNull String origin,@NonNull String destination);
    Either<ServiceError,Route> getRoute(@NonNull Integer id);
    Either<ServiceError,Route> createRoute(@NonNull @Valid RouteForm routeForm);
    Either<ServiceError,Route> patchRoute(@NonNull Integer id, @NonNull @Valid RoutePatch routePatch);
}
