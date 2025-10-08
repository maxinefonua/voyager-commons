package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.RouteQuery;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;

import static org.voyager.utils.Constants.*;

public interface RouteService {
    Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery);
    Either<ServiceError,Route> getRoute(String origin,String destination);
    Either<ServiceError,Route> getRoute(Integer id);
    Either<ServiceError,Route> createRoute(RouteForm routeForm);
    Either<ServiceError,Route> patchRoute(Integer id, RoutePatch routePatch);
}
