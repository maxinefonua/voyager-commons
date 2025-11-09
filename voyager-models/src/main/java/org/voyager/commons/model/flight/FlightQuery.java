package org.voyager.commons.model.flight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.junit.platform.commons.util.StringUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidFlightNumber;
import org.voyager.commons.validate.annotations.ValidNonNullField;

import java.util.List;
import java.util.StringJoiner;

@Getter @SuperBuilder
@Setter
public class FlightQuery {
    @Builder.Default
    @Min(0)
    private int page = 0;

    @Builder.Default
    @Min(1) @Max(100)
    private int pageSize = 20;

    private List<@NotNull Integer> routeIdList;

    private Boolean isActive;

    public String getRequestURL() {
        StringJoiner paramJoiner = new StringJoiner("&");
        if (routeIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            routeIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramJoiner.add(String.format("%s=%s", ParameterNames.ROUTE_ID_PARAM_NAME,routeIdJoiner));
        }
        if (isActive != null) {
            paramJoiner.add(String.format("%s=%s", ParameterNames.IS_ACTIVE_PARAM_NAME,isActive));
        }
        paramJoiner.add(String.format("%s=%s",ParameterNames.PAGE,page));
        paramJoiner.add(String.format("%s=%s",ParameterNames.PAGE_SIZE,pageSize));
        return String.format("%s?%s", Path.FLIGHTS,paramJoiner);
    }
}
