package org.voyager.commons.model.route;

import jakarta.validation.constraints.NotEmpty;
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
    private List<@ValidAirportCode(allowNull = true,
            message = Regex.AIRPORT_CODE) String> originList;

    private List<@ValidAirportCode(allowNull = true,
            message = Regex.AIRPORT_CODE) String> destinationList;

    // TODO: add verification for origin only allows excludeDestination etc
    private Set<@ValidAirportCode String> excludeDestinationSet;
    private Set<Integer> excludeRouteIdSet;

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");
        if (originList != null && !originList.isEmpty()) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN_PARAM_NAME,
                    String.join(",",originList)));
        }

        if (destinationList != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION_PARAM_NAME,
                    String.join(",",destinationList)));
        }

        return String.format("%s?%s",Path.ROUTES,paramsJoiner);
    }
}
