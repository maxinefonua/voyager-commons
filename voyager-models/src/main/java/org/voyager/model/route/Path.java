package org.voyager.model.route;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Path {
    @Builder.Default
    Double totalDistanceKm = 0.0;
    @Builder.Default
    List<RouteAirline> routeAirlineList = new ArrayList<>();
}
