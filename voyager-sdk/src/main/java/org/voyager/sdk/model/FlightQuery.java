package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.junit.platform.commons.util.StringUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.NonNullElements;
import org.voyager.commons.validate.annotations.ValidFlightNumber;
import org.voyager.sdk.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class FlightQuery {
    @Getter
    @NonNullElements(message = "must be a nonempty list of valid route ids") // allows null list
    private List<Integer> routeIdList;

    @Getter
    @ValidFlightNumber(allowNull = true)
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
        if (routeIdList == null && flightNumber == null && airline == null && isActive == null)
            throw new IllegalArgumentException("at least one field of FlightQuery must be set");
    }

    public static FlightQueryBuilder builder() {
        return new FlightQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner paramJoiner = new StringJoiner("&");
        if (routeIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            routeIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramJoiner.add(String.format("%s=%s", ParameterNames.ROUTE_ID_PARAM_NAME,routeIdJoiner));
        }
        if (StringUtils.isNotBlank(flightNumber)) {
            paramJoiner.add(String.format("%s=%s", ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        }
        if (airline != null) {
            paramJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }
        if (isActive != null) {
            paramJoiner.add(String.format("%s=%s", ParameterNames.IS_ACTIVE_PARAM_NAME,isActive));
        }
        return String.format("%s?%s", Path.FLIGHTS,paramJoiner);
    }

    public static class FlightQueryBuilder {
        private List<Integer> routeIdList;
        private String flightNumber;
        private Airline airline;
        private Boolean isActive;

        public FlightQueryBuilder withRouteIdList(@NonNull List<Integer> routeIdList) {
            this.routeIdList = routeIdList;
            return this;
        }

        public FlightQueryBuilder withFlightNumber(@NonNull String flightNumber) {
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
            FlightQuery flightQuery = new FlightQuery(routeIdList,flightNumber,airline,isActive);
            JakartaValidationUtil.validate(flightQuery);
            return flightQuery;
        }
    }
}
