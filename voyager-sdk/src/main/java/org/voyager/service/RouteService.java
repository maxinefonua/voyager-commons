package org.voyager.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.RouteQuery;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import java.util.List;

public interface RouteService {
    Either<ServiceError, List<Route>> getRoutes();
    Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery);
    Either<ServiceError,Route> getRoute(@NonNull String origin,@NonNull String destination);
    Either<ServiceError,Route> getRoute(@NonNull Integer id);
    Either<ServiceError,Route> createRoute(@NonNull @Valid RouteForm routeForm);
    Either<ServiceError,Route> patchRoute(@NonNull Integer id, @NonNull @Valid RoutePatch routePatch);
}
