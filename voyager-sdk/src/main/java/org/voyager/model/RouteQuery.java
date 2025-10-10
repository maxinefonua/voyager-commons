package org.voyager.model;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;
import java.util.StringJoiner;

public class RouteQuery {
    @Getter
    @Pattern(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE) // only checks when nonnull string
    private String origin;

    @Getter
    @Pattern(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE) // only checks when nonnull string
    private String destination;

    @Getter
    private Airline airline;

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
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.ORIGIN_PARAM_NAME,origin));
        }

        if (destination != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.DESTINATION_PARAM_NAME,destination));
        }

        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }
        return String.format("%s?%s",Constants.Voyager.Path.ROUTES,paramsJoiner);
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
            JakartaValidationUtil.validate(routeQuery);
            if (origin != null) routeQuery.origin = origin.toUpperCase();
            if (destination != null) routeQuery.destination = destination.toUpperCase();
            return routeQuery;
        }
    }
}
