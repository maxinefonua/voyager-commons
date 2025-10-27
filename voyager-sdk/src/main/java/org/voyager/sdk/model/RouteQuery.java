package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.ValidationUtils;
import java.util.StringJoiner;

@Getter
public class RouteQuery {
    @ValidAirportCode(allowNull = true,caseSensitive = false,
            message = Regex.AIRPORT_CODE_CASE_INSENSITIVE)
    private String origin;

    @ValidAirportCode(allowNull = true,caseSensitive = false,
            message = Regex.AIRPORT_CODE_CASE_INSENSITIVE)
    private String destination;

    private final Airline airline;

    RouteQuery(String origin, String destination, Airline airline) {
        this.origin = origin;
        this.destination = destination;
        this.airline = airline;
        if (origin == null && destination == null && airline == null)
            throw new IllegalArgumentException("at least one field of RouteQuery must be set");
    }

    public static RouteQueryBuilder builder() {
        return new RouteQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");
        if (origin != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN_PARAM_NAME,origin));
        }

        if (destination != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION_PARAM_NAME,destination));
        }

        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }
        return String.format("%s?%s",Path.ROUTES,paramsJoiner);
    }

    public static class RouteQueryBuilder {
        private String origin;
        private String destination;
        private Airline airline;

        public RouteQueryBuilder withOrigin(@NonNull String origin) {
            this.origin = origin;
            return this;
        }

        public RouteQueryBuilder withDestination(@NonNull String destination) {
            this.destination = destination;
            return this;
        }

        public RouteQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public RouteQuery build() {
            RouteQuery routeQuery = new RouteQuery(origin,destination,airline);
            ValidationUtils.validateAndThrow(routeQuery);
            if (origin != null) routeQuery.origin = origin.toUpperCase();
            if (destination != null) routeQuery.destination = destination.toUpperCase();
            return routeQuery;
        }
    }
}
