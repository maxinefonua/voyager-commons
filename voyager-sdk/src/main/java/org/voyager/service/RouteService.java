package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.route.Path;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.*;

public class RouteService {
    private final String servicePath;
    private final String pathPath;

    RouteService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getRoutesServicePath();
        this.pathPath = voyagerConfig.getPath();
    }

    public Either<ServiceError, List<Route>> getRoutes() {
        return fetch(servicePath, HttpMethod.GET, new TypeReference<List<Route>>() {});
    }

    public Either<ServiceError,Route> createRoute(RouteForm routeForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Route.class,routeForm);
    }

    public Either<ServiceError,List<Route>> getRoutes(String origin,String destination,String airline) {
        String requestURL = servicePath.concat(String.format("?%s=%s&%s=%s&%s=%s",
                ORIGIN_PARAM_NAME,origin,DESTINATION_PARAM_NAME,destination,AIRLINE_PARAM_NAME,airline));
        return fetch(requestURL, HttpMethod.GET, new TypeReference<List<Route>>(){});
    }

    public Either<ServiceError,Route> getRoute(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetch(requestURL,HttpMethod.GET,Route.class);
    }

    public Either<ServiceError,Route> patchRoute(Route route, RoutePatch routePatch) {
        String requestURL = servicePath.concat(String.format("/%d",route.getId()));
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }

    public Either<ServiceError, Path> getPath(String origin, String destination) {
        String requestURL = pathPath.concat(String.format("/%s/to/%s",origin,destination));
        return fetch(requestURL,HttpMethod.GET,Path.class);
    }

    public Either<ServiceError, Path> getPath(String origin, String destination, List<String> excludeAirportList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        excludeAirportList.forEach(stringJoiner::add);
        String requestURL = pathPath.concat(String.format("/%s/to/%s" + "?%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoiner));
        return fetch(requestURL,HttpMethod.GET,Path.class);
    }

    public Either<ServiceError, Path> getPath(String origin, String destination, List<String> excludeAirportList, List<Integer> excludeRouteIdList) {
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = pathPath.concat(String.format("/%s/to/%s" + "?%s=%s" + "&%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoinerAirports,
                EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds));
        return fetch(requestURL,HttpMethod.GET,Path.class);
    }
}
