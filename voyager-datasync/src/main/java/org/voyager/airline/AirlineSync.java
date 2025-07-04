package org.voyager.airline;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.route.Route;
import org.voyager.service.FlightService;
import org.voyager.service.RouteService;
import org.voyager.service.Voyager;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AirlineSync {
    private static Voyager voyager;
    private static FlightService flightService;
    private static RouteService routeService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineSync.class);

    public static void main(String[] args) {
        System.out.println("printing from airline sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Airline airline = datasyncProgramArguments.getAirline();
        VoyagerConfig voyagerConfig = datasyncProgramArguments.getVoyagerConfig();
        Voyager voyager = new Voyager(voyagerConfig);
        flightService = voyager.getFlightService();
        routeService = voyager.getRouteService();

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
        ConstantsLocal.writeSetToFileForDBInsertionWithAirline(airlineCodes,airline,true,ConstantsLocal.FLIGHT_AIRPORTS_FILE);
        ConstantsLocal.writeSetLineByLine(failedFlightNumbers,ConstantsLocal.FAILED_FLIGHT_NUMS_FILE);
    }
}
