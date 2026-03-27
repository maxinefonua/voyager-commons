package org.voyager.sync.service;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.route.Route;
import java.util.List;

public interface RouteProcessor {
    Either<ServiceError, Route> fetchOrCreateRoute(Airport originAirport, Airport destinationAirport);
    List<Route> fetchRoutesToProcess(boolean isRetry);
    Route fetchSavedCivilRoute(String origin, String destination);
}
