package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.route.Route;
import org.voyager.sdk.service.GeoService;
import org.voyager.sdk.service.RouteService;
import org.voyager.sync.model.flightradar.RouteFR;
import org.voyager.sync.service.AirlineProcessor;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.RouteProcessor;
import org.voyager.sync.service.external.FlightRadarService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AirlineProcessorImpl implements AirlineProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineProcessorImpl.class);
    private final AirportReference airportReference;
    private final RouteService routeService;
    private final GeoService geoService;
    private final RouteProcessor routeProcessor;
    public AirlineProcessorImpl(AirportReference airportReference, RouteService routeService, GeoService geoService,
                         RouteProcessor routeProcessor){
        this.routeProcessor = routeProcessor;
        this.airportReference = airportReference;
        this.routeService = routeService;
        this.geoService = geoService;
    }

    @Override
    public void process(Airline airline) {
        fetchAndCreateMissingRoutes(airline);
        fetchAirlineFlightsAndCreateAirlineAirports(airline);
    }

    private void fetchAirlineFlightsAndCreateAirlineAirports(Airline airline) {
    }

    private void fetchAndCreateMissingRoutes(Airline airline) {
        Either<ServiceError, List<RouteFR>> airlineFlightRadarEither = FlightRadarService.extractAirlineRoutes(airline);
        Either<ServiceError, List<Route>> routesEither = routeService.getRoutes();
        if (airlineFlightRadarEither.isLeft()) {
            Exception exception = airlineFlightRadarEither.getLeft().getException();
            LOGGER.error("failed to fetch {} routes from FlightRadar with error {}",
                    airline.name(),exception.getMessage(),exception);
        } else if (routesEither.isLeft()) {
            Exception exception = routesEither.getLeft().getException();
            LOGGER.error("failed to fetch all routes from voyager with error {}",
                    exception.getMessage(),exception);
        } else {
            LOGGER.info("successfully fetched {} routes from voyager API and airline routes from FlightRadar",
                    airline.name());

            AtomicInteger createdRoutes = new AtomicInteger(0);
            AtomicInteger existingRoutes = new AtomicInteger(0);
            AtomicInteger skippedRoutes = new AtomicInteger(0);
            AtomicInteger errorRoutes = new AtomicInteger(0);
            processAirlineRoutes(
                    airlineFlightRadarEither.get(),routesEither.get(),airline,createdRoutes,existingRoutes,
                    skippedRoutes,errorRoutes);
            LOGGER.info("processed {} with {} existing routes, {} skips, {} creates, and {} errors",
                    airline.name(),existingRoutes.get(),skippedRoutes.get(),createdRoutes.get(),errorRoutes.get());
        }
    }

    private void processAirlineRoutes(
            List<RouteFR> routeFRList, List<Route> routeList, Airline airline, AtomicInteger createdRoutes,
            AtomicInteger existingRoutes, AtomicInteger skippedRoutes, AtomicInteger errorRoutes) {
        Map<String, Route> routeMap = routeList.stream()
                .collect(Collectors.toMap(
                        route -> String.format("%s:%s", route.getOrigin(), route.getDestination()),
                        route -> route
                ));
        List<RouteFR> failureRoutes =  routeFRList.stream().filter(routeFR -> {
            String airportCode1 = routeFR.getAirport1().getIata();
            String airportCode2 = routeFR.getAirport2().getIata();
            String key = String.format("%s:%s",airportCode1,airportCode2);
            if (StringUtils.isBlank(airportCode1) || StringUtils.isBlank(airportCode2)
                    || routeMap.containsKey(key) || airportCode1.equals(airportCode2)) {
                existingRoutes.getAndIncrement();
                return false;
            }
            Airport airport1 = null;
            Airport airport2 = null;
            if (airportReference.isSavedAirport(airportCode1)) {
                Option<Airport> airportOption = airportReference.getCivilAirportOption(airportCode1);
                if (airportOption.isDefined()) {
                    airport1 = airportOption.get();
                } else {
                    LOGGER.trace("{} is non-CIVIL airport, skipping route {}",airportCode1,key);
                    skippedRoutes.getAndIncrement();
                    return false;
                }
            } else {
                Either<ServiceError, Airport> createEither =
                        airportReference.addMissingAirport(airportCode1,routeFR.getAirport1(),geoService);
                if (createEither.isLeft()) {
                    Exception exception = createEither.getLeft().getException();
                    LOGGER.error("failed to create {} of {}:{} with error {}",
                            airportCode1,airportCode1,airportCode2,exception.getMessage());
                    errorRoutes.getAndIncrement();
                    return true;
                }
                airport1 = createEither.get();
            }
            if (airportReference.isSavedAirport(airportCode2)) {
                Option<Airport> airportOption = airportReference.getCivilAirportOption(airportCode2);
                if (airportOption.isDefined()) {
                    airport2 = airportOption.get();
                } else {
                    LOGGER.trace("{} is non-CIVIL airport, skipping route {}",airportCode2,key);
                    skippedRoutes.getAndIncrement();
                    return false;
                }
            } else {
                Either<ServiceError,Airport> createEither =
                        airportReference.addMissingAirport(airportCode2,routeFR.getAirport2(),geoService);
                if (createEither.isLeft()) {
                    Exception exception = createEither.getLeft().getException();
                    LOGGER.error("failed to create {} of {}:{} with error {}",
                            airportCode2,airportCode1,airportCode2,exception.getMessage());
                    errorRoutes.getAndIncrement();
                    return true;
                }
                airport2 = createEither.get();
            }
            if (airport1 == null || airport2 == null) {
                LOGGER.error("failed to get both airport1 {} and airport2 {} for route {}, skipping",
                        airport1,airport2,key);
                errorRoutes.getAndIncrement();
                return true;
            }
            if (!airport1.getType().equals(AirportType.CIVIL) || !airport2.getType().equals(AirportType.CIVIL)) {
                LOGGER.trace("route {} contains non-CIVIL airports, skipping route", key);
                skippedRoutes.getAndIncrement();
                return false;
            }
            Either<ServiceError, Route> either = routeProcessor.fetchOrCreateRoute(airport1,airport2);
            if (either.isRight()) {
                createdRoutes.getAndIncrement();
            } else {
                errorRoutes.getAndIncrement();
            }
            return either.isLeft();
        }).toList();
        failureRoutes.forEach(routeFR -> {
            LOGGER.error("failed to create {} route from routeFR: {}",airline.name(),routeFR);
        });
    }
}
