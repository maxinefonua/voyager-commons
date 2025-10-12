package org.voyager.airline;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.RoutesSyncConfig;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportCH;
import org.voyager.model.flightRadar.AirportFR;
import org.voyager.model.flightRadar.RouteFR;
import org.voyager.model.geoname.GeoName;
import org.voyager.model.geoname.Timezone;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.*;
import org.voyager.service.impl.VoyagerServiceRegistry;
import org.voyager.utils.ConstantsDatasync;
import org.voyager.config.DatasyncConfig;

import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.voyager.utils.ConstantsDatasync.MISSING_AIRPORTS_FILE;
import static org.voyager.utils.ConstantsDatasync.ROUTE_AIRPORTS_FILE;

public class RoutesSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesSync.class);
    private static RouteService routeService;
    private static AirportService airportService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        RoutesSyncConfig routesSyncConfig = new RoutesSyncConfig(args);
        Airline airline = routesSyncConfig.getAirline();
        VoyagerConfig voyagerConfig = routesSyncConfig.getVoyagerConfig();
        VoyagerServiceRegistry.initialize(voyagerConfig);
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        routeService = voyagerServiceRegistry.get(RouteService.class);
        airportService = voyagerServiceRegistry.get(AirportService.class);

        Either<ServiceError,List<RouteFR>> either = FlightRadarService.extractAirlineRoutes(airline);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
        } else {
            Set<Airport> missingAirports = new HashSet<>();
            List<RouteFR> docRoutes = either.get();
            processRoutes(docRoutes,missingAirports);
            Set<String> airlineAirports = new HashSet<>();
            docRoutes.forEach(routeFR -> airlineAirports.add(routeFR.getAirport1().getIata()));
            ConstantsDatasync.writeSetLineByLine(airlineAirports,ROUTE_AIRPORTS_FILE);
            if (!missingAirports.isEmpty()) ConstantsDatasync.writeSetToFileForDBInsertionAirports(missingAirports,MISSING_AIRPORTS_FILE);
        }
    }

    private static void processRoutes(List<RouteFR> docRoutes, Set<Airport> missingAirports) {
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
                processRouteFR(routeFR,exists,created,failedRouteStrings,failedFetchAirports,missingAirports));
        LOGGER.info(String.format("completed with existingRoutes count: %d and created count: %d",
                existingRoutes.size(),created.get()));
        failedFetchAirports.forEach(airportFR ->
                LOGGER.error(String.format("failed to fetch airport %s",airportFR)));
        failedRouteStrings.forEach(routeString ->
                LOGGER.error(String.format("failed on route %s",routeString)));
    }

    private static void processRouteFR(RouteFR routeFR, Map<String,Route> exists,
                                       AtomicInteger created,
                                       List<String> failedRouteStrings, List<AirportFR> failedFetchAirports,
                                       Set<Airport> missingAirports) {
        String origin = routeFR.getAirport1().getIata();
        String destination = routeFR.getAirport2().getIata();
        String key = String.format("%s:%s",origin,destination);
        if (StringUtils.isBlank(origin) || StringUtils.isBlank(destination)) {
            failedRouteStrings.add(key);
            return;
        }
        Either<ServiceError, Airport> originEither = airportService.getAirport(origin);
        if (originEither.isLeft()) {
            if (missingAirports.stream().anyMatch(airport -> airport.getIata().equals(origin))) return;
            if (originEither.getLeft().getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                Either<ServiceError, AirportCH> airportEither = ChAviationService.getAirportCH(origin);
                if (airportEither.isLeft()) {
                    Exception exception = airportEither.getLeft().getException();
                    LOGGER.error(exception.getMessage(), exception);
                } else {
                    AirportCH airportCH = airportEither.get();
                    AirportFR airportFR = routeFR.getAirport1();
                    Either<ServiceError, List<GeoName>> either = GeoNamesService.findNearbyPlaces(airportFR.getLat(), airportFR.getLon());
                    if (either.isLeft()) {
                        Exception exception = either.getLeft().getException();
                        LOGGER.error(exception.getMessage(), exception);
                    } else {
                        Airport airport = Airport.builder()
                                .name(airportFR.getName())
                                .countryCode(airportCH.getCountryCode())
                                .iata(airportCH.getIata())
                                .type(airportCH.getType())
                                .longitude(airportFR.getLon())
                                .latitude(airportFR.getLat())
                                .build();
                        if (either.get().isEmpty()) {
                            LOGGER.error(String.format("Missing airport: %s from ChAviationService: %s, from FlightRadar: %s",
                                    airport, airportCH, airportFR));
                        } else {
                            GeoName geoName = either.get().get(0);
                            airport.setCity(geoName.getName());
                            airport.setSubdivision(geoName.getAdminName1());
                            Either<ServiceError, Timezone> timezoneEither = GeoNamesService.getTimezone(
                                    airport.getLatitude(), airport.getLongitude());
                            if (timezoneEither.isLeft()) {
                                Exception exception = timezoneEither.getLeft().getException();
                                LOGGER.error(exception.getMessage(), exception);
                            } else {
                                Timezone timezone = timezoneEither.get();
                                airport.setZoneId(ZoneId.of(timezone.getTimezoneId()));
                                missingAirports.add(airport);
                                LOGGER.error(String.format("Missing airport: %s from ChAviationService: %s, from FlightRadar: %s",
                                        airport, airportCH, airportFR));
                            }
                        }
                    }
                }
            } else {
                Exception exception = originEither.getLeft().getException();
                LOGGER.error(exception.getMessage(), exception);
            }
            failedFetchAirports.add(routeFR.getAirport1());
            return;
        }
        Either<ServiceError, Airport> destinationEither = airportService.getAirport(destination);
        if (destinationEither.isLeft()) {
            if (missingAirports.stream().anyMatch(airport -> airport.getIata().equals(destination))) return;
            if (destinationEither.getLeft().getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                Either<ServiceError, AirportCH> airportEither = ChAviationService.getAirportCH(destination);
                if (airportEither.isLeft()) {
                    Exception exception = airportEither.getLeft().getException();
                    LOGGER.error(exception.getMessage(),exception);
                } else {
                    AirportCH airportCH = airportEither.get();
                    AirportFR airportFR = routeFR.getAirport2();
                    Airport airport = Airport.builder()
                            .name(airportFR.getName())
                            .countryCode(airportCH.getCountryCode())
                            .iata(airportCH.getIata())
                            .type(airportCH.getType())
                            .longitude(airportFR.getLon())
                            .latitude(airportFR.getLat())
                            .build();
                    LOGGER.error(String.format("Missing airport: %s from ChAviationService: %s, from FlightRadar: %s",
                            airport,airportCH,airportFR));
                }
            } else {
                Exception exception = destinationEither.getLeft().getException();
                LOGGER.error(exception.getMessage(), exception);
            }
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