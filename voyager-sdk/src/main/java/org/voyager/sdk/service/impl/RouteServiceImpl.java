package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.RouteQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;

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
        return serviceUtils.fetch(Path.ROUTES,HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    @Override
    public Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery) {
        return serviceUtils.fetch(routeQuery.getRequestURL(),HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    @Override
    public Either<ServiceError, Route> getRoute(@NonNull String origin, @NonNull String destination) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s",
                Path.ROUTE,
                ParameterNames.ORIGIN_PARAM_NAME,origin,
                ParameterNames.DESTINATION_PARAM_NAME,destination);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> getRoute(@NonNull Integer id) {
        String requestURL = String.format("%s/%d",Path.ROUTES,id);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> createRoute(@NonNull @Valid RouteForm routeForm) {
        return serviceUtils.fetchWithRequestBody(Path.Admin.ROUTES,HttpMethod.POST,Route.class,routeForm);
    }

    @Override
    public Either<ServiceError, Route> patchRoute(@NonNull Integer id, @NonNull @Valid RoutePatch routePatch) {
        String requestURL = String.format("%s/%d",Path.Admin.ROUTES,id);
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
