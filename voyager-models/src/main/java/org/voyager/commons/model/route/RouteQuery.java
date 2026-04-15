package org.voyager.commons.model.route;

import lombok.Builder;
import lombok.Data;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
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

    private List<@ValidAirportCode String> excludeAirportList;
    private Set<Integer> excludeRouteIdSet;

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");
        if (originList != null && !originList.isEmpty()) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.ORIGIN,
                    String.join(",",originList)));
        }

        if (destinationList != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.DESTINATION,
                    String.join(",",destinationList)));
        }

        // TODO: add tests for exclusions
        if (excludeAirportList != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE,
                    String.join(",",excludeAirportList)));
        }

        if (excludeRouteIdSet != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.EXCLUDE_ROUTE,
                    String.join(",",excludeRouteIdSet.stream().map(String::valueOf).toList())));
        }

        return String.format("%s?%s",Path.ROUTES,paramsJoiner);
    }
}
