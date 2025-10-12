package org.voyager.airline;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.AirlineSyncConfig;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.route.Route;
import org.voyager.service.FlightService;
import org.voyager.service.RouteService;
import org.voyager.service.impl.VoyagerServiceRegistry;
import org.voyager.utils.ConstantsDatasync;
import org.voyager.config.DatasyncConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AirlineSync {
    private static FlightService flightService;
    private static RouteService routeService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineSync.class);

    public static void main(String[] args) {
        System.out.println("printing from airline sync main");
        AirlineSyncConfig airlineSyncConfig = new AirlineSyncConfig(args);
        Airline airline = airlineSyncConfig.getAirline();
        VoyagerConfig voyagerConfig = airlineSyncConfig.getVoyagerConfig();
        VoyagerServiceRegistry.initialize(voyagerConfig);
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        flightService = voyagerServiceRegistry.get(FlightService.class);
        routeService = voyagerServiceRegistry.get(RouteService.class);

        Either<ServiceError, List<Flight>> flightsEither = flightService.getFlights();
        if (flightsEither.isLeft()) {
            Exception exception = flightsEither.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
            return;
        }

        List<Flight> activeAirlineFlights = flightsEither.get().stream()
                .filter(flight -> flight.getIsActive()
                        && flight.getAirline().equals(airline)
                        && flight.getZonedDateTimeDeparture() != null
                        && flight.getZonedDateTimeArrival() != null)
                .toList();
        Set<String> airlineCodes = new HashSet<>();
        Set<String> failedFlightNumbers = new HashSet<>();
        activeAirlineFlights.forEach(flight -> {
            Either<ServiceError, Route> routeEither = routeService.getRoute(flight.getRouteId());
            if (routeEither.isLeft()) {
                Exception exception = routeEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                failedFlightNumbers.add(flight.getFlightNumber());
                return;
            }
            Route route = routeEither.get();
            airlineCodes.add(route.getOrigin());
            airlineCodes.add(route.getDestination());
        });
        ConstantsDatasync.writeSetToFileForDBInsertionWithAirline(airlineCodes,airline,true, ConstantsDatasync.FLIGHT_AIRPORTS_FILE);
        ConstantsDatasync.writeSetLineByLine(failedFlightNumbers, ConstantsDatasync.FAILED_FLIGHT_NUMS_FILE);
    }
}
