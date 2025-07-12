package org.voyager.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.Airline;

import java.util.ArrayList;
import java.util.List;

@Data @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class PathAirline {
    Airline airline;
    @Builder.Default
    List<Route> routeList = new ArrayList<>();
}
