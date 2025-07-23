package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.location.Location;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.*;

public class RouteService {
    private final String servicePath;

    RouteService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getRoutesServicePath();
    }

    public Either<ServiceError, List<Route>> getRoutes() {
        return fetch(servicePath, HttpMethod.GET, new TypeReference<List<Route>>() {});
    }

    public Either<ServiceError,Route> createRoute(RouteForm routeForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Route.class,routeForm);
    }

    public Either<ServiceError,List<Route>> getRoutes(String origin,String destination) {
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,origin,DESTINATION_PARAM_NAME,destination));
        return fetch(requestURL, HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    public Either<ServiceError,Route> getRoute(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetch(requestURL,HttpMethod.GET,Route.class);
    }

    public Either<ServiceError,Route> patchRoute(Integer id, RoutePatch routePatch) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
