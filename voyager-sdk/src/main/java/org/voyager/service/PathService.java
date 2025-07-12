package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.route.Path;
import org.voyager.model.route.PathAirline;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.utils.ConstantsUtils.*;

public class PathService {
    private final String serviceAirlinePath;
    private final String servicePath;

    PathService(@NonNull VoyagerConfig voyagerConfig) {
        this.serviceAirlinePath = voyagerConfig.getPathServiceAirlinePath();
        this.servicePath = voyagerConfig.getPathServicePath();
    }

    public Either<ServiceError, List<PathAirline>> getPathAirlineList(String origin, String destination) {
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s",origin,destination));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<PathAirline>> getPathAirlineList(String origin, String destination, Airline airline) {
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s" + "?%s=%s",
                origin,destination,AIRLINE_PARAM_NAME,airline));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<PathAirline>> getPathAirlineList(String origin, String destination,
                                                                     Airline airline, List<String> excludeAirportCodeList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoiner::add);
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s" + "?%s=%s" + "&%s=%s",
                origin,destination,AIRLINE_PARAM_NAME,airline,EXCLUDE_PARAM_NAME,stringJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<PathAirline>> getPathAirlineList(String origin, String destination,
                                                                     List<String> excludeAirportCodeList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoiner::add);
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s" + "?%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<PathAirline>> getPathAirlineList(String origin, String destination,
                                                                     List<String> excludeAirportCodeList,
                                                                     List<Integer> excludeRouteIdList) {
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s" + "?%s=%s" + "&%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoinerAirports,
                EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<Path>> getPathList(String origin, String destination,
                                                       List<String> excludeAirportCodeList,
                                                       List<Integer> excludeRouteIdList) {
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = servicePath.concat(String.format("/%s/to/%s" + "?%s=%s" + "&%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoinerAirports,
                EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Path>>(){});
    }

    public Either<ServiceError,List<Path>> getPathList(String origin, String destination) {
        String requestURL = servicePath.concat(String.format("/%s/to/%s", origin,destination));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Path>>(){});
    }


    public Either<ServiceError,List<PathAirline>> getPathAirlineList(String origin, String destination,
                                                                     List<String> excludeAirportList,
                                                                     List<Integer> excludeRouteIdList,
                                                                     Airline airline) {
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = serviceAirlinePath.concat(String.format("/%s/to/%s" + "?%s=%s" + "&%s=%s" + "&%s=%s",
                origin,destination,EXCLUDE_PARAM_NAME,stringJoinerAirports,
                EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds,
                AIRLINE_PARAM_NAME,airline));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }
}
