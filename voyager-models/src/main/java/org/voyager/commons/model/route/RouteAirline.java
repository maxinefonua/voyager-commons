package org.voyager.commons.model.route;

import lombok.Builder;
import lombok.Data;
import org.voyager.commons.model.airline.Airline;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class RouteAirline {
    Integer routeId;
    String origin;
    String destination;
    @Builder.Default
    Double distanceKm = 0.0;
    @Builder.Default
    List<Airline> airlines = new ArrayList<>();
}
