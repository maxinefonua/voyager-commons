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
import org.voyager.model.route.PathResponse;

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

    public Either<ServiceError,List<Path>> getPathList(List<String> originList, List<String> destinationList) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Path>>(){});
    }

    public Either<ServiceError, PathResponse<PathAirline>> getPathAirlineList(List<String> originList, List<String> destinationList) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        String requestURL = serviceAirlinePath.concat(String.format("?%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<PathResponse<PathAirline>>(){});
    }

    public Either<ServiceError,PathResponse<PathAirline>> getPathAirlineList(List<String> originList, List<String> destinationList, Airline airline) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        String requestURL = serviceAirlinePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner,AIRLINE_PARAM_NAME,airline.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<PathResponse<PathAirline>>(){});
    }

    public Either<ServiceError,List<PathAirline>> getPathAirlineList(List<String> originList, List<String> destinationList,
                                                                     Airline airline, List<String> excludeAirportCodeList) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        StringJoiner excludeAirportJoiner = new StringJoiner(",");
        excludeAirportCodeList.forEach(excludeAirportJoiner::add);
        String requestURL = serviceAirlinePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner,AIRLINE_PARAM_NAME,airline.name(),
                EXCLUDE_PARAM_NAME,excludeAirportJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<PathAirline>>(){});
    }

    public Either<ServiceError,List<Path>> getPathList(List<String> originList, List<String> destinationList,
                                                       List<String> excludeAirportCodeList,
                                                       List<Integer> excludeRouteIdList) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner,
                EXCLUDE_PARAM_NAME,stringJoinerAirports, EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Path>>(){});
    }

    public Either<ServiceError,PathResponse<PathAirline>> getPathAirlineList(List<String> originList, List<String> destinationList,
                                                                     List<String> excludeAirportCodeList,
                                                                     List<Integer> excludeRouteIdList) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportCodeList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = serviceAirlinePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner,
                EXCLUDE_PARAM_NAME,stringJoinerAirports,EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<PathResponse<PathAirline>>(){});
    }


    public Either<ServiceError, PathResponse<PathAirline>> getPathAirlineList(List<String> originList, List<String> destinationList,
                                                                              List<String> excludeAirportList,
                                                                              List<Integer> excludeRouteIdList,
                                                                              Airline airline) {
        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        StringJoiner stringJoinerAirports = new StringJoiner(",");
        excludeAirportList.forEach(stringJoinerAirports::add);
        StringJoiner stringJoinerRouteIds = new StringJoiner(",");
        excludeRouteIdList.forEach(routeId -> stringJoinerRouteIds.add(String.valueOf(routeId)));
        String requestURL = serviceAirlinePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                ORIGIN_PARAM_NAME,originJoiner,DESTINATION_PARAM_NAME,destinationJoiner,
                EXCLUDE_PARAM_NAME,stringJoinerAirports, EXCLUDE_ROUTE_PARAM_NAME,stringJoinerRouteIds,
                AIRLINE_PARAM_NAME,airline));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<PathResponse<PathAirline>>(){});
    }
}
