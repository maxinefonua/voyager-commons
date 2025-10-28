package org.voyager.sdk.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.*;
import org.voyager.commons.validate.ValidationUtils;
import java.util.List;
import java.util.StringJoiner;

@Getter
public class AirlinePathQuery {
    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> originIATAList;

    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> destinationIATAList;

    private final Airline airline;

    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> excludeIATAList;

    private final List<@NotNull Integer> excludeRouteIdList;

    private List<@ValidFlightNumber(message = Regex.ConstraintMessage.FLIGHT_NUMBER_ELEMENTS)
            String> excludeFlightNumberList;

    @Min(1)
    @Max(15)
    private final Integer limit;

    AirlinePathQuery(@NonNull List<String> originIATAList, @NonNull List<String> destinationIATAList, Airline airline,
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

    public String getRequestURL() {
        StringJoiner originJoiner = new StringJoiner(",");
        originIATAList.forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationIATAList.forEach(destinationJoiner::add);

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }

        if (excludeRouteIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        if (excludeIATAList != null) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeIATAList.forEach(routeId -> iataJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        if (excludeFlightNumberList != null) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberList.forEach(routeId -> flightJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        if (limit != null) paramsJoiner.add(String.format("%s=%d", ParameterNames.LIMIT_PARAM_NAME,limit));

        return String.format("%s?%s",Path.AIRLINE_PATH,paramsJoiner);
    }

    public static PathAirlineQueryBuilder builder() {
        return new PathAirlineQueryBuilder();
    }

    public static class PathAirlineQueryBuilder {
        private List<String> originIATAList;
        private List<String> destinationIATAList;
        private Airline airline;
        private List<String> excludeIATAList;
        private List<Integer> excludeRouteIdList;
        private List<String> excludeFlightNumberList;
        private Integer limit;

        public PathAirlineQueryBuilder withOriginIATAList(@NonNull List<String> originIATAList) {
            this.originIATAList = originIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withDestinationIATAList(@NonNull List<String> destinationIATAList) {
            this.destinationIATAList = destinationIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeIATAList(@NonNull List<String> excludeIATAList) {
            this.excludeIATAList = excludeIATAList;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeRouteIdList(@NonNull List<Integer> excludeRouteIdList) {
            this.excludeRouteIdList = excludeRouteIdList;
            return this;
        }

        public PathAirlineQueryBuilder withExcludeFlightNumberList(@NonNull List<String> excludeFlightNumberList) {
            this.excludeFlightNumberList = excludeFlightNumberList;
            return this;
        }

        public PathAirlineQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public AirlinePathQuery build() {
            AirlinePathQuery airlinePathQuery = new AirlinePathQuery(originIATAList,destinationIATAList,
                    airline,excludeIATAList,excludeRouteIdList,excludeFlightNumberList,limit);
            ValidationUtils.validateAndThrow(airlinePathQuery);
            airlinePathQuery.originIATAList = originIATAList.stream().map(String::toUpperCase).toList();
            airlinePathQuery.destinationIATAList = destinationIATAList.stream()
                    .map(String::toUpperCase).toList();
            if (excludeIATAList != null) {
                airlinePathQuery.excludeIATAList = excludeIATAList.stream().map(String::toUpperCase).toList();
            }
            if (excludeFlightNumberList != null) {
                airlinePathQuery.excludeFlightNumberList = excludeFlightNumberList.stream()
                        .map(String::toUpperCase).toList();
            }
            return airlinePathQuery;
        }
    }
}
