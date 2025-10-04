package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

public class RouteService {
    private static final String ROUTES_PATH = "/routes";
    private final ServiceUtils serviceUtils;

    RouteService() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    RouteService(ServiceUtils serviceUtils) {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    public Either<ServiceError, List<Route>> getRoutes() {
        return serviceUtils.fetch(ROUTES_PATH, HttpMethod.GET, new TypeReference<List<Route>>() {});
    }

    public Either<ServiceError,Route> createRoute(RouteForm routeForm) {
        return serviceUtils.fetchWithRequestBody(ROUTES_PATH,HttpMethod.POST,Route.class,routeForm);
    }

    public Either<ServiceError,List<Route>> getRoutes(String origin,String destination) {
        String requestURL = ROUTES_PATH.concat(String.format("?%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,origin,DESTINATION_PARAM_NAME,destination));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    public Either<ServiceError,Route> getRoute(Integer id) {
        String requestURL = ROUTES_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Route.class);
    }

    public Either<ServiceError,Route> patchRoute(Integer id, RoutePatch routePatch) {
        String requestURL = ROUTES_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
