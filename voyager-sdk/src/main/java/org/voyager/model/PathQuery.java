package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class PathQuery {
    private List<String> originIATAList;
    private List<String> destinationIATAList;
    private List<String> excludeIATAList;
    private List<Integer> excludeRouteIdList;
    private List<String> excludeFlightNumberList;
    private Integer limit;

    PathQuery(@NonNull List<String> originIATAList, @NonNull List<String> destinationIATAList,
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

    public static String resolveRequestURL(@NonNull PathQuery pathQuery) {
        StringJoiner originJoiner = new StringJoiner(",");
        pathQuery.originIATAList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        pathQuery.destinationIATAList.forEach(destinationJoiner::add);

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        List<String> excludeIATAList = pathQuery.excludeIATAList;
        if (excludeIATAList != null && !excludeIATAList.isEmpty()) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeIATAList.forEach(iataJoiner::add);
            paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        List<Integer> excludeRouteIdList = pathQuery.excludeRouteIdList;
        if (excludeRouteIdList != null && !excludeRouteIdList.isEmpty()) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",Constants.Voyager.ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        List<String> excludeFlightNumberList = pathQuery.excludeFlightNumberList;
        if (excludeFlightNumberList != null && !excludeFlightNumberList.isEmpty()) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberList.forEach(flightJoiner::add);
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        Integer limit = pathQuery.limit;
        if (limit != null) paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));

        return String.format("%s?%s",Constants.Voyager.Path.PATH,paramsJoiner);
    }

    public static class PathQueryBuilder {
        private List<String> originIATAList;
        private List<String> destinationIATAList;
        private List<String> excludeIATAList;
        private List<Integer> excludeRouteIdList;
        private List<String> excludeFlightNumberList;
        private Integer limit;

        public PathQueryBuilder withOriginIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> originIATAList) {
            this.originIATAList = originIATAList;
            return this;
        }

        public PathQueryBuilder withDestinationIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> destinationIATAList) {
            this.destinationIATAList = destinationIATAList;
            return this;
        }

        public PathQueryBuilder withExcludeIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> excludeIATAList) {
            this.excludeIATAList = excludeIATAList;
            return this;
        }

        public PathQueryBuilder withExcludeRouteIdList(@NotEmpty @Valid List<@NonNull Integer> excludeRouteIdList) {
            this.excludeRouteIdList = excludeRouteIdList;
            return this;
        }

        public PathQueryBuilder withExcludeFlightNumberList(@NotEmpty @Valid List<@NotBlank String>
                                                                    excludeFlightNumberList) {
            this.excludeFlightNumberList = excludeFlightNumberList;
            return this;
        }

        public PathQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public PathQuery build() {
            return new PathQuery(originIATAList,destinationIATAList,excludeIATAList,excludeRouteIdList,
                    excludeFlightNumberList,limit);
        }
    }
}
