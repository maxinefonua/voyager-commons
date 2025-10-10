package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.RouteQuery;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.RouteService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;

public class RouteServiceImpl implements RouteService {
    private final ServiceUtils serviceUtils;

    RouteServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    RouteServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<Route>> getRoutes() {
        return serviceUtils.fetch(Constants.Voyager.Path.ROUTES,HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    @Override
    public Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery) {
        return serviceUtils.fetch(routeQuery.getRequestURL(),HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    @Override
    public Either<ServiceError, Route> getRoute(@NonNull String origin, @NonNull String destination) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s",
                Constants.Voyager.Path.ROUTE,
                Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,origin,
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destination);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> getRoute(@NonNull Integer id) {
        String requestURL = String.format("%s/%d",Constants.Voyager.Path.ROUTES,id);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> createRoute(@NonNull @Valid RouteForm routeForm) {
        return serviceUtils.fetchWithRequestBody(Constants.Voyager.Path.ROUTES,HttpMethod.POST,Route.class,routeForm);
    }

    @Override
    public Either<ServiceError, Route> patchRoute(@NonNull Integer id, @NonNull @Valid RoutePatch routePatch) {
        String requestURL = String.format("%s/%d",Constants.Voyager.Path.ROUTES,id);
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
