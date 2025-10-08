package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import org.voyager.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class PathAirlineQuery {
    private List<String> originIATAList;
    private List<String> destinationIATAList;
    private Airline airline;
    private List<String> excludeIATAList;
    private List<Integer> excludeRouteIdList;
    private List<String> excludeFlightNumberList;
    private Integer limit;

    PathAirlineQuery(@NonNull List<String> originIATAList, @NonNull List<String> destinationIATAList, Airline airline,
                     List<String> excludeIATAList, List<Integer> excludeRouteIdList,
                     List<String> excludeFlightNumberList, Integer limit) {
        this.originIATAList = originIATAList;
        this.destinationIATAList = destinationIATAList;
        this.airline = airline;
        this.excludeIATAList = excludeIATAList;
        this.excludeRouteIdList = excludeRouteIdList;
        this.excludeFlightNumberList = excludeFlightNumberList;
        this.limit = limit;
    }

    public static String resolveRequestURL(@NonNull PathAirlineQuery pathAirlineQuery) {
        List<String> originIATAList = pathAirlineQuery.originIATAList;
        List<String> destinationIATAList = pathAirlineQuery.destinationIATAList;
        StringJoiner originJoiner = new StringJoiner(",");
        originIATAList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationIATAList.forEach(destinationJoiner::add);

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        Airline airline = pathAirlineQuery.airline;
        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }

        List<Integer> excludeRouteIdList = pathAirlineQuery.excludeRouteIdList;
        if (excludeRouteIdList != null && !excludeRouteIdList.isEmpty()) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        List<String> excludeIATAList = pathAirlineQuery.excludeIATAList;
        if (excludeIATAList != null && !excludeIATAList.isEmpty()) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeIATAList.forEach(routeId -> iataJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        List<String> excludeFlightNumberList = pathAirlineQuery.excludeFlightNumberList;
        if (excludeFlightNumberList != null && !excludeFlightNumberList.isEmpty()) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberList.forEach(routeId -> flightJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        Integer limit = pathAirlineQuery.limit;
        if (limit != null) paramsJoiner.add(String.format("%s=%d",
                Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));

        return String.format("%s?%s",Constants.Voyager.Path.PATH_AIRLINE,paramsJoiner);
    }

    public static PathAirlineQueryBuilder builder() {
        return new PathAirlineQueryBuilder();
    }

    public static class PathAirlineQueryBuilder {
        private List<String> originIATAList;
        private List<String> destinationIATAList = new ArrayList<>();
        private Airline airline;
        private List<String> excludeIATAList;
        private List<Integer> excludeRouteIdList;
        private List<String> excludeFlightNumberList;
        private Integer limit;

        public PathAirlineQueryBuilder withOriginIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> originIATAList) {
            this.originIATAList = originIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withDestinationIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> destinationIATAList) {
            this.destinationIATAList = destinationIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeIATAList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> excludeIATAList) {
            this.excludeIATAList = excludeIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeRouteIdList(@NotEmpty @Valid List<@NonNull Integer> excludeRouteIdList) {
            this.excludeRouteIdList = excludeRouteIdList;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeFlightNumberList(@NotEmpty @Valid List<@NotBlank String>
                                                                           excludeFlightNumberList) {
            this.excludeFlightNumberList = excludeFlightNumberList;
            return this;
        }

        public PathAirlineQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public PathAirlineQuery build() {
            return new PathAirlineQuery(originIATAList,destinationIATAList,airline,excludeIATAList,excludeRouteIdList,
                    excludeFlightNumberList,limit);
        }
    }
}
