package org.voyager.commons.model.path;

import lombok.Builder;
import lombok.Data;
import org.voyager.commons.model.route.Route;
import java.util.List;

@Builder @Data
public class Path {
    private List<Route> routeList;
    private Double totalDistanceKm;
}
