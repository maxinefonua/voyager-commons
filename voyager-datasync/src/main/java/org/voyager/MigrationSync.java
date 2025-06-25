package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.route.Route;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.FlightService;
import org.voyager.service.RouteService;
import org.voyager.service.Voyager;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MigrationSync {
    private static FlightService flightService;
    private static RouteService routeService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationSync.class);

    public static void main(String[] args) {
        System.out.println("printing from airline sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,
                maxConcurrentRequests,voyagerAuthorizationToken);
        Voyager voyager = new Voyager(voyagerConfig);
        flightService = voyager.getFlightService();
        routeService = voyager.getRouteService();

        migrateFlightIdsToRoutes();
    }

    private static void migrateFlightIdsToRoutes() {
        AtomicInteger proccessed = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        Either<ServiceError, List<Flight>> flightsEither = flightService.getFlights();
        if (flightsEither.isLeft()) {
            Exception exception = flightsEither.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
            errors.getAndIncrement();
            return;
        }
        List<Flight> flights = flightsEither.get();
        flights.forEach(flight -> {
            Either<ServiceError, Route> routeEither = routeService.getRoute(flight.getRouteId());
            if (routeEither.isLeft()) {
                Exception exception = routeEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                errors.getAndIncrement();
                return;
            }
            Route route = routeEither.get();
            if (route.getFlightIds().contains(flight.getId())) return;
            List<Integer> flightIds = route.getFlightIds();
            flightIds.add(flight.getId());
            RoutePatch routePatch = RoutePatch.builder().flightIds(flightIds).build();
            Either<ServiceError, Route> patchEither = routeService.patchRoute(route,routePatch);
            if (patchEither.isLeft()) {
                Exception exception = patchEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                errors.getAndIncrement();
                return;
            }
            Route patched = patchEither.get();
            LOGGER.info(String.format("successfully patched route %d with flight id %d: %s",
                    patched.getId(),flight.getId(),patched));
            proccessed.getAndIncrement();
        });
        LOGGER.info(String.format("completed with %d flights successfully processed, and %d with errors",
                proccessed.get(),errors.get()));
    }
}
