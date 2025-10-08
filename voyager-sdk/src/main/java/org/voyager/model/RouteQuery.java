package org.voyager.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import org.voyager.utils.Constants;

import java.util.StringJoiner;

public class RouteQuery {
    private String origin;
    private String destination;
    private Airline airline;

    RouteQuery(String origin, String destination, Airline airline) {
        this.origin = origin;
        this.destination = destination;
        this.airline = airline;
    }

    public static RouteQueryBuilder builder() {
        return new RouteQueryBuilder();
    }

    public static String resolveRequestURL(RouteQuery routeQuery) {
        if (routeQuery == null) return Constants.Voyager.Path.ROUTES;
        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,routeQuery.origin));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,routeQuery.destination));

        if (routeQuery.airline != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,routeQuery.airline.name()));
        }
        if (paramsJoiner.length() == 0) return Constants.Voyager.Path.ROUTES;
        return String.format("%s?%s",Constants.Voyager.Path.ROUTES,paramsJoiner);
    }

    public static class RouteQueryBuilder {
        private String origin;
        private String destination;
        private Airline airline;

        public RouteQueryBuilder withOrigin(@NonNull @Pattern(regexp = Constants.Voyager.Regex.ALPHA3_CODE_REGEX,
                message = Constants.Voyager.ConstraintMessage.IATA_CODE) String origin) {
            this.origin = origin;
            return this;
        }

        public RouteQueryBuilder withDestination(@NonNull @Pattern(regexp = Constants.Voyager.Regex.ALPHA3_CODE_REGEX,
                message = Constants.Voyager.ConstraintMessage.IATA_CODE) String destination) {
            this.destination = destination;
            return this;
        }

        public RouteQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public RouteQuery build() {
            return new RouteQuery(origin,destination,airline);
        }
    }
}
