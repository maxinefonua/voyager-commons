package org.voyager.sync.service;

import org.voyager.commons.model.route.Route;

import java.util.List;

public interface FlightProcessor {
    void process(List<Route> routeList);
}
