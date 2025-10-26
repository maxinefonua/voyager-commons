package org.voyager.commons.model.route;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class RoutePath {
    @Builder.Default
    Double totalDistanceKm = 0.0;
    @Builder.Default
    List<RouteAirline> routeAirlineList = new ArrayList<>();
}
