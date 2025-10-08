package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import org.junit.platform.commons.util.StringUtils;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class FlightQuery {
    @Getter
    private List<Integer> routeIdList;
    @Getter
    private String flightNumber;
    @Getter
    private Airline airline;
    @Getter
    private Boolean isActive;

    FlightQuery(List<Integer> routeIdList,String flightNumber,Airline airline,Boolean isActive) {
        this.routeIdList = routeIdList;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.isActive = isActive;
    }

    public static FlightQueryBuilder builder() {
        return new FlightQueryBuilder();
    }

    public static String resolveRequestURL(FlightQuery flightQuery) {
        if (flightQuery == null) return Constants.Voyager.Path.FLIGHTS;
        StringJoiner paramJoiner = new StringJoiner("&");
        List<Integer> routeIdList = flightQuery.getRouteIdList();
        if (routeIdList != null && !routeIdList.isEmpty()) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            routeIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.ROUTE_ID_PARAM_NAME,routeIdJoiner));
        }
        String flightNumber = flightQuery.getFlightNumber();
        if (StringUtils.isNotBlank(flightNumber)) {
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        }
        Airline airline = flightQuery.getAirline();
        if (airline != null) {
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }
        Boolean isActive = flightQuery.getIsActive();
        if (isActive != null) {
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.IS_ACTIVE_PARAM_NAME,isActive));
        }
        if (paramJoiner.length() == 0) return Constants.Voyager.Path.FLIGHTS;
        return String.format("%s?%s", Constants.Voyager.Path.FLIGHTS,paramJoiner);
    }

    public static class FlightQueryBuilder {
        private List<Integer> routeIdList;
        private String flightNumber;
        private Airline airline;
        private Boolean isActive;

        public FlightQueryBuilder withRouteIdList(@NotEmpty @Valid List<@NonNull Integer> routeIdList) {
            this.routeIdList = routeIdList;
            return this;
        }

        public FlightQueryBuilder withFlightNumber(@NotBlank String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public FlightQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public FlightQueryBuilder withActive(@NonNull Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlightQuery build() {
            return new FlightQuery(routeIdList,flightNumber,airline,isActive);
        }
    }
}
