package org.voyager.model.route;

import lombok.Builder;
import lombok.Data;
import org.voyager.model.Airline;

import java.util.ArrayList;
import java.util.List;

@Data @Builder(toBuilder = true)
public class PathAirline {
    @Builder.Default
    List<Route> routeList = new ArrayList<>();
    Airline airline;
}
