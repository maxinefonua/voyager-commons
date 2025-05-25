package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpFactory;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.voyager.service.ServiceUtils.responseBody;
import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithPayload;
import static org.voyager.utils.ConstantsUtils.*;

public class RouteService {
    private final String servicePath;
    private final VoyagerHttpFactory voyagerHttpFactory;
    private final ObjectMapper om = new ObjectMapper();
    private final URI serviceURI;

    RouteService(@NonNull VoyagerConfig voyagerConfig, @NonNull VoyagerHttpFactory voyagerHttpFactory) {
        this.servicePath = voyagerConfig.getRoutesServicePath();
        this.voyagerHttpFactory = voyagerHttpFactory;
        try {
            this.serviceURI = new URI(servicePath);
        } catch (URISyntaxException e) { // TODO: correct exception to throw here?
            throw new IllegalArgumentException(String.format("Exception thrown while building URI of service path '%s'",servicePath),e);
        }
    }

    public Either<ServiceError, List<Route>> getRoutes() {
        return fetch(servicePath, HttpMethod.GET, new TypeReference<List<Route>>() {});
    }

    public Either<ServiceError,Route> createRoute(RouteForm routeForm) {
        return fetchWithPayload(servicePath,HttpMethod.POST,Route.class,routeForm);
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
        return fetchWithPayload(requestURL,HttpMethod.PATCH,Route.class,routePatch);
    }
}
