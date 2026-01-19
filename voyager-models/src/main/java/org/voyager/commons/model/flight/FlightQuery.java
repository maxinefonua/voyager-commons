package org.voyager.commons.model.flight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import java.time.ZonedDateTime;
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

    @NotNull
    private ZonedDateTime startTime;
    @NotNull
    private ZonedDateTime endTime;

    private Boolean isActive;

    public String getRequestURL() {
        StringJoiner paramJoiner = new StringJoiner("&");
        if (routeIdList != null) {
            StringJoiner routeIdJoiner = new StringJoiner(",");
            routeIdList.forEach(routeId -> routeIdJoiner.add(String.valueOf(routeId)));
            paramJoiner.add(String.format("%s=%s", ParameterNames.ROUTE_ID,routeIdJoiner));
        }
        if (isActive != null) {
            paramJoiner.add(String.format("%s=%s", ParameterNames.IS_ACTIVE,isActive));
        }
        paramJoiner.add(String.format("%s=%s",ParameterNames.PAGE,page));
        paramJoiner.add(String.format("%s=%s",ParameterNames.SIZE,pageSize));
        paramJoiner.add(String.format("%s=%s",ParameterNames.START,startTime));
        paramJoiner.add(String.format("%s=%s",ParameterNames.END,endTime));
        return String.format("%s?%s", Path.FLIGHTS,paramJoiner);
    }
}
