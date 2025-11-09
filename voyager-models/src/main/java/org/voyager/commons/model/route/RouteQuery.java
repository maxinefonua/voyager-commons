package org.voyager.commons.model.route;

import lombok.Builder;
import lombok.Data;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.annotations.ValidNonNullField;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@Builder @Data @ValidNonNullField
public class RouteQuery {
    @ValidAirportCode(allowNull = true,
            message = Regex.AIRPORT_CODE)
    private String origin;

    @ValidAirportCode(allowNull = true,
            message = Regex.AIRPORT_CODE)
    private String destination;

    // TODO: add verification for origin only allows excludeDestination etc
    private Set<@ValidAirportCode String> excludeDestinationSet;
    private Set<Integer> excludeRouteIdSet;

    private Airline airline;

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
}
