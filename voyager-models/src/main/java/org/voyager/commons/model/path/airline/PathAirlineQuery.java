package org.voyager.commons.model.path.airline;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.*;

import java.util.Set;
import java.util.StringJoiner;

@Data @Builder
public class PathAirlineQuery {
    @Builder.Default
    @Min(0)
    private int page = 0;

    @Builder.Default
    @Min(1) @Max(10)
    private int size = 5;

    private @NotEmpty
    Set<@ValidAirportCode(message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS)
            String> originSet;

    @NotEmpty
    private Set<@ValidAirportCode(message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS)
            String> destinationSet;

    private Airline airline;

    private Set<@ValidAirportCode(message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS)
            String> excludeSet;

    private Set<@NotNull Integer> excludeRouteIdSet;

    private Set<@ValidFlightNumber(message = Regex.ConstraintMessage.FLIGHT_NUMBER_ELEMENTS)
            String> excludeFlightNumberSet;

    public String getRequestURL() {
        StringJoiner originJoiner = new StringJoiner(",");
        originSet.stream().sorted().forEach(originJoiner::add);
        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationSet.stream().sorted().forEach(destinationJoiner::add);

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN_PARAM_NAME,originJoiner));
        paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION_PARAM_NAME,destinationJoiner));

        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }

        if (excludeRouteIdSet != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            excludeRouteIdSet.stream().sorted()
                    .forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_ROUTE_PARAM_NAME,routeIdJoiner));
        }

        if (excludeSet != null) {
            StringJoiner iataJoiner = new StringJoiner(",");
            excludeSet.stream().sorted()
                    .forEach(routeId -> iataJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_PARAM_NAME,iataJoiner));
        }

        if (excludeFlightNumberSet != null) {
            StringJoiner flightJoiner = new StringJoiner(",");
            excludeFlightNumberSet.stream().sorted()
                    .forEach(routeId -> flightJoiner.add(String.valueOf(routeId)));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_FLIGHT_PARAM_NAME,flightJoiner));
        }

        paramsJoiner.add(String.format("%s=%d", ParameterNames.PAGE,page));
        paramsJoiner.add(String.format("%s=%d", ParameterNames.PAGE_SIZE,size));
        return String.format("%s?%s",Path.AIRLINE_PATH,paramsJoiner);
    }
}
