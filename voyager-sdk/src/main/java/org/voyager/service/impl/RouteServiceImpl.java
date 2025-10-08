package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
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
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    @Override
    public Either<ServiceError, List<Route>> getRoutes(RouteQuery routeQuery) {
        String requestURL = RouteQuery.resolveRequestURL(routeQuery);
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    @Override
    public Either<ServiceError, Route> getRoute(String origin, String destination) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s",
                Constants.Voyager.Path.ROUTE,
                Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,origin,
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destination);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> getRoute(Integer id) {
        String requestURL = Constants.Voyager.Path.ROUTES.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    @Override
    public Either<ServiceError, Route> createRoute(RouteForm routeForm) {
        return serviceUtils.fetchWithRequestBody(Constants.Voyager.Path.ROUTES,HttpMethod.POST,Route.class,routeForm);
    }

    @Override
    public Either<ServiceError, Route> patchRoute(Integer id, RoutePatch routePatch) {
        String requestURL = Constants.Voyager.Path.ROUTES.concat(String.format("/%d",id));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
