package org.voyager.model;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.validate.annotations.AllStringsMatchRegex;
import org.voyager.model.validate.annotations.NonNullElements;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;

import java.util.List;
import java.util.StringJoiner;

public class RoutePathQuery {
    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS) // allows null List, excludes null elements
    private List<String> originIATAList;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS) // allows null List, excludes null elements
    private List<String> destinationIATAList;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS) // allows null List, excludes null elements
    private List<String> excludeIATAList;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @NonNullElements
    private List<Integer> excludeRouteIdList;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.NOEMPTY_NOWHITESPACE,
            message = Constants.Voyager.ConstraintMessage.NOEMPTY_NOWHITESPACE) // allows null List, excludes null elements
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
        paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        if (excludeIATAList != null) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeIATAList.forEach(iataJoiner::add);
            paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        if (excludeRouteIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        if (excludeFlightNumberList != null) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberList.forEach(flightJoiner::add);
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        if (limit != null) paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));

        return String.format("%s?%s",Constants.Voyager.Path.ROUTE_PATH,paramsJoiner);
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
