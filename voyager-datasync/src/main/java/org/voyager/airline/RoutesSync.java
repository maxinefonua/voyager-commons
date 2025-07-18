package org.voyager.airline;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.datasync.AirportFR;
import org.voyager.model.datasync.RouteFR;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.AirportService;
import org.voyager.service.FlightRadarService;
import org.voyager.service.RouteService;
import org.voyager.service.Voyager;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.voyager.utils.ConstantsLocal.ROUTE_AIRPORTS_FILE;

public class RoutesSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesSync.class);
    private static RouteService routeService;
    private static AirportService airportService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Airline airline = datasyncProgramArguments.getAirline();
        VoyagerConfig voyagerConfig = datasyncProgramArguments.getVoyagerConfig();
        Voyager voyager = new Voyager(voyagerConfig);
        routeService = voyager.getRouteService();
        airportService = voyager.getAirportService();

        Either<ServiceError,List<RouteFR>> either = FlightRadarService.extractAirlineRoutes(airline);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
        } else {
            List<RouteFR> docRoutes = either.get();
            processRoutes(docRoutes);
            Set<String> airlineAirports = new HashSet<>();
            docRoutes.forEach(routeFR -> airlineAirports.add(routeFR.getAirport1().getIata()));
            ConstantsLocal.writeSetLineByLine(airlineAirports, ROUTE_AIRPORTS_FILE);
        }
    }

    private static void processRoutes(List<RouteFR> docRoutes) {
        Either<ServiceError,List<Route>> getEither = routeService.getRoutes();
        if (getEither.isLeft()) {
            Exception exception = getEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }

        List<Route> existingRoutes = getEither.get();
        List<String> failedRouteStrings = new ArrayList<>();
        List<AirportFR> failedFetchAirports = new ArrayList<>();
        Map<String,Route> exists = new HashMap<>();
        existingRoutes.forEach(route ->
                exists.put(String.format("%s:%s",route.getOrigin(),route.getDestination()),route));
        AtomicInteger created = new AtomicInteger(0);
        docRoutes.forEach(routeFR ->
                processRouteFR(routeFR,exists,created,failedRouteStrings,failedFetchAirports));
        LOGGER.info(String.format("completed with existingRoutes count: %d and created count: %d",
                existingRoutes.size(),created.get()));
        failedFetchAirports.forEach(airportFR ->
                LOGGER.error(String.format("failed to fetch airport %s",airportFR)));
        failedRouteStrings.forEach(routeString ->
                LOGGER.error(String.format("failed on route %s",routeString)));
    }

    private static void processRouteFR(RouteFR routeFR, Map<String,Route> exists,
                                       AtomicInteger created,
                                       List<String> failedRouteStrings, List<AirportFR> failedFetchAirports) {
        String origin = routeFR.getAirport1().getIata();
        String destination = routeFR.getAirport2().getIata();
        String key = String.format("%s:%s",origin,destination);
        if (StringUtils.isBlank(origin) || StringUtils.isBlank(destination)) {
            failedRouteStrings.add(key);
            return;
        }
        Either<ServiceError, Airport> originEither = airportService.getAirport(origin);
        if (originEither.isLeft()) {
            Exception exception = originEither.getLeft().getException();
            LOGGER.error(exception.getMessage(), exception);
            failedFetchAirports.add(routeFR.getAirport1());
            return;
        }
        Either<ServiceError, Airport> destinationEither = airportService.getAirport(destination);
        if (destinationEither.isLeft()) {
            Exception exception = destinationEither.getLeft().getException();
            LOGGER.error(exception.getMessage(), exception);
            failedFetchAirports.add(routeFR.getAirport2());
            return;
        }
        Airport originAirport = originEither.get();
        Airport destinationAirport = destinationEither.get();
        Double distanceKm = Airport.calculateDistanceKm(originAirport.getLatitude(),originAirport.getLongitude(),
                destinationAirport.getLatitude(),destinationAirport.getLongitude());
        if (exists.containsKey(key)) {
            Route route = exists.get(key);
            if (route.getDistanceKm() == null || !route.getDistanceKm().equals(distanceKm)) {
                // patch route
                Either<ServiceError, Route> either = routeService.patchRoute(route.getId(),RoutePatch.builder().distanceKm(distanceKm).build());
                if (either.isLeft()) {
                    Exception exception = either.getLeft().getException();
                    LOGGER.error(String.format("patch route's distanceKm failed, added to failedRouteStrings. error: %s",exception.getMessage()),exception);
                    failedRouteStrings.add(key);
                } else {
                    LOGGER.info("successfully patched existing route: " + either.get());
                }
            } else LOGGER.info("skipped matching route: " + exists.get(key));
        } else {
            RouteForm routeForm = RouteForm.builder()
                    .origin(origin)
                    .destination(destination)
                    .distanceKm(distanceKm)
                    .build();
            Either<ServiceError, Route> createEither = routeService.createRoute(routeForm);
            if (createEither.isLeft()) {
                Exception exception = createEither.getLeft().getException();
                LOGGER.error(exception.getMessage(), exception);
                failedRouteStrings.add(key);
            } else {
                LOGGER.info("successfully created route: " + createEither.get());
                created.getAndIncrement();
            }
        }
    }
}