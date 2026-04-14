package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;
import org.voyager.commons.model.flight.FlightUpsert;
import org.voyager.commons.model.route.Route;
import org.voyager.sdk.service.FlightService;
import org.voyager.sync.model.flightradar.search.AirlineFR;
import org.voyager.sync.model.flightradar.search.AirportScheduleFR;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.AirportScheduleProcessor;
import org.voyager.sync.service.RouteProcessor;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportScheduleProcessorImpl implements AirportScheduleProcessor {
    private final AirportReference airportReference;
    private final FlightService flightService;
    private final RouteProcessor routeProcessor;
    private final List<Airline> airlineList;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportScheduleProcessorImpl.class);

    AirportScheduleProcessorImpl(
            FlightService flightService, AirportReference airportReference, RouteProcessor routeProcessor,
            List<Airline> airlineList) {
        this.flightService = flightService;
        this.airportReference = airportReference;
        this.routeProcessor = routeProcessor;
        if (airlineList == null || airlineList.isEmpty()) {
            List<Airline> airlines = new ArrayList<>(Arrays.stream(Airline.values()).toList());
            airlines.sort(Comparator.comparing(Airline::name));
            this.airlineList = airlines;
        } else {
            this.airlineList = airlineList;
        }
    }

    @Override
    public Either<AirportScheduleFailure, AirportScheduleResult> process(
            AirportScheduleFR airportScheduleFR, String airportCode1, String airportCode2) {
        boolean noDepartures = airportScheduleFR.getDepartures() == null
                || airportScheduleFR.getDepartures().isEmpty();
        boolean noArrivals = airportScheduleFR.getArrivals() == null || airportScheduleFR.getArrivals().isEmpty();
        if (noArrivals && noDepartures) {
            return Either.right(new AirportScheduleResult(
                    airportCode1,airportCode2,0,0, 0));
        }
        Set<Airline> airlineSet = new HashSet<>();
        AtomicInteger flightCreates = new AtomicInteger(0);
        AtomicInteger flightSkips = new AtomicInteger(0);
        AtomicInteger flightPatches = new AtomicInteger(0);
        List<FlightUpsert> flightUpsertList = new ArrayList<>();

        Airport airport1 = airportReference.getCivilAirportOption(airportCode1).get();
        Airport airport2 = airportReference.getCivilAirportOption(airportCode2).get();
        if (airport1 == null || airport2 == null) {
            return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,
                    new ServiceError(HttpStatus.NOT_FOUND,
                            new ServiceException("airports not found civil airport map in voyager reference"))));
        }
        if (airportScheduleFR.getArrivals() != null && !airportScheduleFR.getArrivals().isEmpty()) {
            Either<ServiceError, Route> processingEither = routeProcessor.fetchOrCreateRoute(airport1,airport2);
            if (processingEither.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,processingEither.getLeft()));
            }
            Integer routeId = processingEither.get().getId();
            airportScheduleFR.getArrivals().forEach((countryName, countryFR) ->
                    countryFR.getIataToFlightsMap().get(airportCode2).getFlightNumberToPlannedMap()
                            .forEach((flightNumber,plannedFR)->{
                                AirlineFR airlineFR = plannedFR.getAirline();
                                try {
                                    Airline airline = Airline.fromPathVariableFR(airlineFR.getUrl());
                                    if (!airlineList.contains(airline)) {
                                        LOGGER.debug("skipping flight of excluded airline {}",airline.name());
                                        return;
                                    }
                                    airlineSet.add(airline);
                                    FlightUpsert flightUpsert = FlightUpsert.builder()
                                            .isArrival(String.valueOf(true))
                                            .airline(airline.name())
                                            .flightNumber(flightNumber)
                                            .routeId(String.valueOf(routeId))
                                            .zonedDateTimeList(plannedFR.getDateToTimeMap().values().stream()
                                                    .map(flightTimeFR -> {
                                                        Instant instant = Instant.ofEpochSecond(
                                                                flightTimeFR.getTimestamp());
                                                        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(
                                                                flightTimeFR.getOffset());
                                                        return ZonedDateTime.ofInstant(instant, zoneOffset);
                                                    }).toList())
                                            .build();
                                    flightUpsertList.add(flightUpsert);
                                } catch (IllegalArgumentException e) {
                                    LOGGER.trace("ignoring unmapped airline {} flight",airlineFR.getName());
                                }
                            }));
        }
        // TODO: redo routes. routes must be created ONLY when there are existing flights. but when reverse routes are created, they need to be added to process list. so maybe add reverse route AHEAD of time
        if (airportScheduleFR.getDepartures() != null && !airportScheduleFR.getDepartures().isEmpty()) {
            Either<ServiceError, Route> processingEither = routeProcessor.fetchOrCreateRoute(airport2,airport1);
            if (processingEither.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,processingEither.getLeft()));
            }
            Integer routeId = processingEither.get().getId();
            airportScheduleFR.getDepartures().forEach((countryName, countryFR) ->
                    countryFR.getIataToFlightsMap().get(airportCode2).getFlightNumberToPlannedMap()
                            .forEach((flightNumber, plannedFR) -> {
                                AirlineFR airlineFR = plannedFR.getAirline();
                                try {
                                    Airline airline = Airline.fromPathVariableFR(airlineFR.getUrl());
                                    if (!airlineList.contains(airline)) {
                                        LOGGER.info("skipping flight of excluded airline {}", airline.name());
                                        return;
                                    }
                                    airlineSet.add(airline);

                                    FlightUpsert flightUpsert = FlightUpsert.builder()
                                            .isArrival(String.valueOf(false))
                                            .airline(airline.name())
                                            .flightNumber(flightNumber)
                                            .routeId(String.valueOf(routeId))
                                            .zonedDateTimeList(plannedFR.getDateToTimeMap().values()
                                                    .stream().map(flightTimeFR -> {
                                                        Instant instant = Instant.ofEpochSecond(
                                                                flightTimeFR.getTimestamp());
                                                        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(
                                                                flightTimeFR.getOffset());
                                                        return ZonedDateTime.ofInstant(instant, zoneOffset);
                                                    }).toList())
                                            .build();
                                    flightUpsertList.add(flightUpsert);
                                } catch (IllegalArgumentException e) {
                                    LOGGER.trace("ignoring unmapped airline {} flight", airlineFR.getName());
                                }
                            }));
        }
        if (flightUpsertList.isEmpty()) {
            LOGGER.info("no flight upserts for {}:{}",airportCode1,airportCode2);
        } else {
            long start = System.currentTimeMillis();
            Either<ServiceError, FlightBatchUpsertResult> either = flightService.batchUpsert(
                    FlightBatchUpsert.builder().flightUpsertList(flightUpsertList).build());
            LOGGER.trace("batchUpsert returned after {}ms",System.currentTimeMillis()-start);
            if (either.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,either.getLeft()));
            } else {
                FlightBatchUpsertResult batchResult = either.get();
                flightPatches.getAndAdd(batchResult.getUpdatedCount());
                flightSkips.getAndAdd(batchResult.getSkippedCount());
                flightCreates.getAndAdd(batchResult.getCreatedCount());
            }
        }
        return Either.right(new AirportScheduleResult(
                airportCode1,airportCode2,flightCreates.get(),flightPatches.get(),flightSkips.get()));
    }
}
