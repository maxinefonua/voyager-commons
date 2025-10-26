package org.voyager.sdk.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.NonNullElements;
import org.voyager.commons.validate.annotations.ValidAirportCodeCollection;
import org.voyager.commons.validate.annotations.ValidFlightNumberCollection;
import org.voyager.sdk.utils.JakartaValidationUtil;

import java.util.List;
import java.util.StringJoiner;

public class RoutePathQuery {
    @Getter
    @ValidAirportCodeCollection(allowNullCollection = false,
            allowEmptyCollection = false,
            caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE)
    private List<String> originIATAList;

    @Getter
    @ValidAirportCodeCollection(allowNullCollection = false,
            allowEmptyCollection = false,
            caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE)
    private List<String> destinationIATAList;

    @Getter
    @ValidAirportCodeCollection(allowNullCollection = true,
            allowEmptyCollection = false,
            caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
    private List<String> excludeIATAList;

    @Getter
    @NonNullElements(message = "must be a nonempty list of valid route ids to exclude")
    private final List<Integer> excludeRouteIdList;

    @Getter
    @ValidFlightNumberCollection(allowNullCollection = true,
            allowEmptyCollection = false,
            message = Regex.ConstraintMessage.FLIGHT_NUMBER_ELEMENTS_NONEMPTY)
    private List<String> excludeFlightNumberList;

    @Getter
    @Min(1) @Max(25)
    private Integer limit;

    RoutePathQuery(@NonNull List<String> originIATAList, @NonNull List<String> destinationIATAList,
                   List<String> excludeIATAList, List<Integer> excludeRouteIdList, List<String> excludeFlightNumberList,
                   Integer limit) {
        this.originIATAList = originIATAList;
        this.destinationIATAList = destinationIATAList;
        this.excludeIATAList = excludeIATAList;
        this.excludeRouteIdList = excludeRouteIdList;
        this.excludeFlightNumberList = excludeFlightNumberList;
        this.limit = limit;
    }

    public static PathQueryBuilder builder() {
        return new PathQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner originJoiner = new StringJoiner(",");
        originIATAList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationIATAList.forEach(destinationJoiner::add);

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        if (excludeIATAList != null) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeIATAList.forEach(iataJoiner::add);
            paramsJoiner.add(String.format("%s=%s",ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        if (excludeRouteIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        if (excludeFlightNumberList != null) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberList.forEach(flightJoiner::add);
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        if (limit != null) paramsJoiner.add(String.format("%s=%s", ParameterNames.LIMIT_PARAM_NAME,limit));

        return String.format("%s?%s",Path.ROUTE_PATH,paramsJoiner);
    }

    public static class PathQueryBuilder {
        private List<String> originIATAList;
        private List<String> destinationIATAList;
        private List<String> excludeIATAList;
        private List<Integer> excludeRouteIdList;
        private List<String> excludeFlightNumberList;
        private Integer limit;

        public PathQueryBuilder withOriginIATAList(@NonNull List<String> originIATAList) {
            this.originIATAList = originIATAList;
            return this;
        }

        public PathQueryBuilder withDestinationIATAList(@NonNull List<String> destinationIATAList) {
            this.destinationIATAList = destinationIATAList;
            return this;
        }

        public PathQueryBuilder withExcludeIATAList(@NonNull List<String> excludeIATAList) {
            this.excludeIATAList = excludeIATAList;
            return this;
        }

        public PathQueryBuilder withExcludeRouteIdList(@NonNull List<Integer> excludeRouteIdList) {
            this.excludeRouteIdList = excludeRouteIdList;
            return this;
        }

        public PathQueryBuilder withExcludeFlightNumberList(@NonNull List<String> excludeFlightNumberList) {
            this.excludeFlightNumberList = excludeFlightNumberList;
            return this;
        }

        public PathQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public RoutePathQuery build() {
            RoutePathQuery routePathQuery = new RoutePathQuery(originIATAList,destinationIATAList,excludeIATAList,
                    excludeRouteIdList, excludeFlightNumberList,limit);
            JakartaValidationUtil.validate(routePathQuery);
            routePathQuery.originIATAList = originIATAList.stream().map(String::toUpperCase).toList();
            routePathQuery.destinationIATAList = destinationIATAList.stream().map(String::toUpperCase).toList();
            if (excludeIATAList != null) {
                routePathQuery.excludeIATAList = excludeIATAList.stream().map(String::toUpperCase).toList();
            }
            if (excludeFlightNumberList != null) {
                routePathQuery.excludeFlightNumberList = excludeFlightNumberList.stream().map(String::toUpperCase).toList();
            }
            return routePathQuery;
        }
    }
}
