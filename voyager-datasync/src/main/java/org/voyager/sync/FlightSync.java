package org.voyager.sync;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.IataQuery;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airport.Airport;
import org.voyager.sync.model.chaviation.AirportCH;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;
import org.voyager.sync.model.flightradar.AirportFR;
import org.voyager.sync.model.flightradar.RouteFR;
import org.voyager.sync.model.flightradar.search.AirlineFR;
import org.voyager.sync.model.flightradar.search.AirportScheduleFR;
import org.voyager.sync.model.flightradar.search.FlightTimeFR;
import org.voyager.sync.model.flightradar.search.PlannedFR;
import org.voyager.sync.model.flights.AirlineRouteResult;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.sync.reference.VoyagerReference;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.AirlineService;
import org.voyager.sdk.service.FlightService;
import org.voyager.sync.service.FlightRadarService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sync.service.ChAviationService;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.utils.ConstantsDatasync;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import static io.vavr.control.Either.right;

public class FlightSync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static AirlineService airlineService;
    private static FlightService flightService;
    private static AirportService airportService;
    private static GeoService geoService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);
    private static final VoyagerReference voyagerReference = new VoyagerReference();

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        init(args);
        Map<String, Set<String>> routeMap = fetchRouteMap();
        Map<Airline,Set<String>> airlineMap = processAndBuildAirlineMap(routeMap,
                executorService,flightService,routeService,airportService);
        processAirlineMap(airlineMap);
        shutdown();
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    public static Map<Airline, Set<String>> processAndBuildAirlineMap(Map<String, Set<String>> routeMap,
                                                                      ExecutorService executorService,
                                                                      FlightService flightService,
                                                                      RouteService routeService,
                                                                      AirportService airportService) {
        List<Future<Either<AirportScheduleFailure,AirportScheduleResult>>> futureList = new ArrayList<>();
        CompletionService<Either<AirportScheduleFailure,AirportScheduleResult>> completionService =
                new ExecutorCompletionService<>(executorService);

        routeMap.forEach((airportCode1,destinationSet)->{
            destinationSet.forEach(airportCode2 -> {
                Callable<Either<AirportScheduleFailure,AirportScheduleResult>> airportScheduleTask = ()->
                        FlightRadarService.extractAirportResponseWithRetry(airportCode1,airportCode2)
                                .mapLeft(serviceError ->
                                        new AirportScheduleFailure(airportCode1,airportCode2,serviceError))
                                .flatMap(
                                        airportScheduleFROption -> {
                                            if (airportScheduleFROption.isEmpty()) {
                                                LOGGER.info("{}:{} returned no flights",airportCode1,airportCode2);
                                                return right(new AirportScheduleResult(airportCode1,airportCode2,
                                                        0,0,0,Set.of(),List.of()));
                                            }
                                            return processAirportSchedule(airportCode1,airportCode2,
                                                    airportScheduleFROption.get(),flightService,routeService,
                                                    airportService);
                                        }
                                );
                futureList.add(completionService.submit(airportScheduleTask));
            });
        });

        int totalTasks = futureList.size();
        int completedTasks = 0;
        int processingErrors = 0;
        int flightCreates = 0;
        int flightPatches = 0;
        int flightSkips = 0;
        List<AirportScheduleFailure> failureList = new ArrayList<>();
        List<AirportScheduleResult> failedFlightNumberList = new ArrayList<>();
        Map<Airline,Set<String>> airlineToIataMap = new HashMap<>();

        while (completedTasks < totalTasks) {
            try {
                Future<Either<AirportScheduleFailure,AirportScheduleResult>> future = completionService.take();
                Either<AirportScheduleFailure,AirportScheduleResult> either = future.get();
                completedTasks++;
                if (either.isLeft()) {
                    AirportScheduleFailure failure = either.getLeft();
                    failureList.add(failure);
                    LOGGER.error("task {}/{} failed for route {}:{} with error: {}", completedTasks,totalTasks,
                            failure.airportCode1,failure.airportCode2,
                            failure.serviceError.getException().getMessage());
                } else {
                    AirportScheduleResult result = either.get();
                    result.airlineSet.forEach(airline -> {
                        Set<String> airlineAirports = airlineToIataMap.getOrDefault(airline,new HashSet<>());
                        airlineAirports.add(result.airportCode1);
                        airlineAirports.add(result.airportCode2);
                        airlineToIataMap.put(airline,airlineAirports);
                    });
                    if (!result.flightNumberErrors.isEmpty()) {
                        failedFlightNumberList.add(result);
                    }
                    flightCreates += result.flightsCreated;
                    flightPatches += result.flightsPatched;
                    flightSkips += result.flightsSkipped;
                    LOGGER.info("task {}/{} completed for route {}:{} with {} creates, {} patches, {} skips and {} failed flights",
                            completedTasks,totalTasks, result.airportCode1,result.airportCode2,
                            result.flightsCreated,result.flightsPatched,result.flightsSkipped,result.flightNumberErrors.size());
                }
            } catch (InterruptedException | ExecutionException e) {
                processingErrors++;
                completedTasks++;
                LOGGER.error("task {}/{} failed with error: {}",completedTasks,totalTasks,e.getMessage());
            }
        }
        LOGGER.info("*****************************************");
        LOGGER.info("completed {}/{} tasks with {} total flight creates, {} flight patches, {} flight skips - {} route task failures, {} processing errors",
                completedTasks,totalTasks,flightCreates,flightPatches,flightSkips,failureList.size(),processingErrors);
        printAndSaveErrors(failedFlightNumberList,failureList);
        return airlineToIataMap;
    }

    private static Either<AirportScheduleFailure,AirportScheduleResult> processAirportSchedule(String airportCode1,
                                                                                                 String airportCode2,
                                                                                                 AirportScheduleFR airportScheduleFR,
                                                                                                 FlightService flightService,
                                                                                                 RouteService routeService,
                                                                                                 AirportService airportService) {
        List<String> failedFlightNumbers = new ArrayList<>();
        Set<Airline> airlineSet = new HashSet<>();
        AtomicInteger flightCreates = new AtomicInteger(0);
        AtomicInteger flightSkips = new AtomicInteger(0);
        AtomicInteger flightPatches = new AtomicInteger(0);
        if (airportScheduleFR.getArrivals() == null || airportScheduleFR.getArrivals().isEmpty()) {
            LOGGER.info("processing {}:{}, no arrivals at {} from {}",
                    airportCode1, airportCode2, airportCode2, airportCode1);
        } else {
            Either<ServiceError, Route> either = fetchOrCreateRoute(airportCode1,airportCode2,routeService,
                    airportService);
            if (either.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,either.getLeft()));
            }
            Integer routeId = either.get().getId();
            airportScheduleFR.getArrivals().forEach((countryName, countryFR) -> {
                countryFR.getIataToFlightsMap().get(airportCode2).getFlightNumberToPlannedMap()
                        .forEach((flightNumber,plannedFR)->{
                            AirlineFR airlineFR = plannedFR.getAirline();
                            try {
                                Airline airline = Airline.fromPathVariableFR(airlineFR.getUrl());
                                if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)
                                        && !flightSyncConfig.getAirlineList().contains(airline)) {
                                    LOGGER.debug("in {} mode, skipping flight of excluded airline {}",
                                            flightSyncConfig.getSyncMode().name(),airline.name());
                                    return;
                                }
                                Either<String, Integer> processEither = processFlightArrivalAt(airportCode2,
                                        airportCode1, airline, airlineSet, flightNumber, plannedFR, routeId,
                                        flightService);
                                if (processEither.isLeft()) {
                                    failedFlightNumbers.add(processEither.getLeft());
                                } else {
                                    int value = processEither.get();
                                    if (value > 0) flightCreates.getAndIncrement();
                                    else if (value < 0) flightPatches.getAndIncrement();
                                    else flightSkips.getAndIncrement();
                                }
                            } catch (IllegalArgumentException e) {
                                LOGGER.trace("ignoring unmapped airline {} flight",airlineFR.getName());
                            }
                        });
            });
        }

        if (airportScheduleFR.getDepartures() == null || airportScheduleFR.getDepartures().isEmpty()) {
            LOGGER.info("processing {}:{}, no departures from {} to {}",
                    airportCode1, airportCode2, airportCode2, airportCode1);
        } else {
            Either<ServiceError, Route> either = fetchOrCreateRoute(airportCode2,airportCode1,routeService,
                    airportService);
            if (either.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,either.getLeft()));
            }
            Integer routeId = either.get().getId();
            airportScheduleFR.getDepartures().forEach((countryName, countryFR) -> {
                countryFR.getIataToFlightsMap().get(airportCode2).getFlightNumberToPlannedMap()
                        .forEach((flightNumber,plannedFR)->{
                            AirlineFR airlineFR = plannedFR.getAirline();
                            try {
                                Airline airline = Airline.fromPathVariableFR(airlineFR.getUrl());
                                if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)
                                        && !flightSyncConfig.getAirlineList().contains(airline)) {
                                    LOGGER.info("in {} mode, skipping flight of excluded airline {}",
                                            flightSyncConfig.getSyncMode().name(),airline.name());
                                    return;
                                }
                                Either<String,Integer> processEither = processFlightDepartureFrom(airportCode2,
                                        airportCode1,airline,airlineSet,flightNumber,plannedFR,routeId,flightService);
                                if (processEither.isLeft()) {
                                    failedFlightNumbers.add(processEither.getLeft());
                                } else {
                                    int value = processEither.get();
                                    if (value > 0) flightCreates.getAndIncrement();
                                    else if (value < 0) flightPatches.getAndIncrement();
                                    else flightSkips.getAndIncrement();
                                }
                            } catch (IllegalArgumentException e) {
                                LOGGER.trace("ignoring unmapped airline {} flight",airlineFR.getName());
                            }
                        });
            });
        }
        return Either.right(new AirportScheduleResult(airportCode1,airportCode2,flightCreates.get(),flightPatches.get(),
                flightSkips.get(),airlineSet,failedFlightNumbers));
    }

    private static Either<String, Integer> processFlightDepartureFrom(String origin, String destination,
                                                                      Airline airline, Set<Airline> airlineSet,
                                                                      String flightNumber, PlannedFR plannedFR,
                                                                      Integer routeId,FlightService flightService) {
        return processFlight(origin,destination,false,airline,airlineSet,flightNumber,plannedFR, routeId,
                flightService);
    }


    private static Either<String, Integer> processFlight(String origin, String destination, boolean isArrival,
                                                         Airline airline, Set<Airline> airlineSet,
                                                         String flightNumber, PlannedFR plannedFR, Integer routeId,
                                                         FlightService flightService) {
        String latestDate = plannedFR.getDateToTimeMap().keySet()
                .stream().max(Comparator.naturalOrder()).orElse(null);
        if (StringUtils.isBlank(latestDate)) {
            LOGGER.error("no latest date for flight {}, route {}->{}, plannedFR: {}",
                    flightNumber,origin,destination,plannedFR);
            return Either.left(flightNumber);
        }

        FlightTimeFR flightTimeFR = plannedFR.getDateToTimeMap().get(latestDate);
        Long arrivalTimestamp = isArrival ? flightTimeFR.getTimestamp() : null;
        Long arrivalOffset = isArrival ? flightTimeFR.getOffset() : null;
        Long departureTimestamp = isArrival ? null : flightTimeFR.getTimestamp();
        Long departureOffset = isArrival ? null : flightTimeFR.getOffset();

        Either<ServiceError, Flight> flightEither = flightService.getFlight(routeId,flightNumber);
        if (flightEither.isLeft()) {
            ServiceError serviceError = flightEither.getLeft();
            if (!serviceError.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                LOGGER.error("failed to fetch flight {} for route {}->{}, error: {}",
                        flightNumber,origin,destination,serviceError.getException().getMessage());
                return Either.left(flightNumber);
            }
            // create flight
            FlightForm flightForm = FlightForm.builder().flightNumber(flightNumber).airline(airline.name())
                    .arrivalOffset(arrivalOffset).arrivalTimestamp(arrivalTimestamp).departureOffset(departureOffset)
                    .departureTimestamp(departureTimestamp).routeId(routeId).isActive(String.valueOf(false))
                    .build();
            Either<ServiceError, Flight> createEither = flightService.createFlight(flightForm);
            if (createEither.isLeft()) {
                LOGGER.error("failed to create flight {} with route {}->{}, error: {}",
                        flightNumber,origin,destination,createEither.getLeft().getException().getMessage());
                return Either.left(flightNumber);
            }
            LOGGER.debug("successfully created flight: {}",createEither.get());
            return right(1);
        }
        Flight flight = flightEither.get();
        boolean isActive = (isArrival && flight.getZonedDateTimeDeparture() != null)
                || (!isArrival && flight.getZonedDateTimeArrival() != null);
        if (flight.getIsActive() == isActive) {
            // skip with no patches
            LOGGER.trace("skipping flight with route {}->{} due to no new data, existing: {}",
                    origin,destination,flight);
            if (isActive && airline != null) {
                airlineSet.add(airline);
            }
            return right(0);
        }

        FlightPatch flightPatch = FlightPatch.builder().arrivalOffset(arrivalOffset)
                .arrivalTimestamp(arrivalTimestamp).departureTimestamp(departureTimestamp)
                .departureOffset(departureOffset).isActive(String.valueOf(isActive)).build();
        Either<ServiceError, Flight> patchEither = flightService.patchFlight(flight.getId(),flightPatch);
        if (patchEither.isLeft()) {
            LOGGER.error("failed to patch flight {} with route {}->{}, error: {}",
                    flightNumber,origin,destination,patchEither.getLeft().getException().getMessage());
            return Either.left(flightNumber);
        }
        if (isActive) {
            airlineSet.add(airline);
        }
        LOGGER.info("successfully patched flight: {}",patchEither.get());
        return right(-1);
    }

    private static Either<String, Integer> processFlightArrivalAt(String destination, String origin,
                                                                  Airline airline, Set<Airline> airlineSet,
                                                                  String flightNumber, PlannedFR plannedFR,
                                                                  Integer routeId,FlightService flightService) {
        return processFlight(origin,destination,true,airline,airlineSet,flightNumber,plannedFR,routeId,
                flightService);
    }

    private static Either<ServiceError, Route> fetchOrCreateRoute(String origin, String destination,
                                                                  RouteService routeService,
                                                                  AirportService airportService) {
        Either<ServiceError, Route> either = routeService.getRoute(origin,destination);
        if (either.isRight()) return either;

        ServiceError serviceError = either.getLeft();
        if (!serviceError.getHttpStatus().equals(HttpStatus.NOT_FOUND)) return either;

        Either<ServiceError, Airport> originEither = airportService.getAirport(origin);
        if (originEither.isLeft()) return Either.left(originEither.getLeft());
        Airport originAirport = originEither.get();

        Either<ServiceError, Airport> destinationEither = airportService.getAirport(destination);
        if (destinationEither.isLeft()) return Either.left(destinationEither.getLeft());
        Airport destinationAirport = destinationEither.get();

        Double distanceKM = Airport.calculateDistanceKm(originAirport.getLatitude(),originAirport.getLongitude(),
                destinationAirport.getLatitude(),destinationAirport.getLongitude());

        RouteForm routeForm = RouteForm.builder().origin(origin).destination(destination)
                .distanceKm(distanceKM).build();
        return routeService.createRoute(routeForm).map(route -> {
            LOGGER.info("successfully created route: {}",route);
            return route;
        });
    }

    private static void printAndSaveErrors(List<AirportScheduleResult> failedFlightNumberList,
                                           List<AirportScheduleFailure> failureList) {
        Set<String> failedFlightNumbers = new HashSet<>();
        failedFlightNumberList.forEach(airportScheduleResult -> {
            LOGGER.error("route {}:{} had {} flight number errors",
                    airportScheduleResult.airportCode1,airportScheduleResult.airportCode2,
                    airportScheduleResult.flightNumberErrors.size());
            StringJoiner flightJoiner = new StringJoiner(",");
            airportScheduleResult.flightNumberErrors.forEach(flightNumber -> {
                LOGGER.info(flightNumber);
                flightJoiner.add(flightNumber);
            });
            failedFlightNumbers.add(String.format("%s:%s %s",
                    airportScheduleResult.airportCode1,airportScheduleResult.airportCode2,flightJoiner));
            LOGGER.info("-----------------------");
        });
        ConstantsDatasync.writeSetLineByLineDirectFile(failedFlightNumbers,flightSyncConfig.getFileName());

        Set<String> failedRoutes = new HashSet<>();
        failureList.forEach(airportScheduleFailure -> {
            LOGGER.error("{}:{} failed with error: {}",airportScheduleFailure.airportCode1,
                    airportScheduleFailure.airportCode2,
                    airportScheduleFailure.serviceError.getException().getMessage());
            failedRoutes.add(String.format("%s:%s",
                    airportScheduleFailure.airportCode1,airportScheduleFailure.airportCode2));
        });
        ConstantsDatasync.writeSetLineByLineDirectFile(failedRoutes,flightSyncConfig.getFileName());
        if (failedRoutes.isEmpty() && failedFlightNumbers.isEmpty()) {
            if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)) {
                for (Airline airline : flightSyncConfig.getAirlineList()) {
                    Either<ServiceError, Integer> either = flightService.batchDelete(FlightBatchDelete.builder().airline(airline.name())
                            .isActive("false").build());
                    if (either.isLeft()) {
                        LOGGER.error("batch DELETE airline {} failed with error: {}",airline.name(),
                                either.getLeft().getException().getMessage());
                    } else {
                        LOGGER.info("successful batch DELETE airline {} with {} records",airline.name(),either.get());
                    }
                }
            } else {
                Either<ServiceError, Integer> either = flightService.batchDelete(FlightBatchDelete.builder()
                        .isActive("false").build());
                if (either.isLeft()) {
                    LOGGER.error("batch DELETE of all inactive flights failed with error: {}",
                            either.getLeft().getException().getMessage());
                } else {
                    LOGGER.info("successful batch DELETE of all inactive flights with {} records",either.get());
                }
            }
        }
    }


    private static void processAirlineMap(Map<Airline, Set<String>> airlineMap) {
        airlineMap.forEach((airline,iataCodes) ->{
            if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.FULL_SYNC)) {
                Either<ServiceError, Integer> either = airlineService.batchDeleteAirline(airline);
                if (either.isLeft()) {
                    LOGGER.error("failed to batch DELETE airline {} with error: {}",
                            airline,either.getLeft().getException().getMessage());
                    return;
                } else {
                    LOGGER.info("successful batch DELETE airline {} of {} records",airline,either.get());
                }
            }
            AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().airline(airline.name())
                    .isActive(true).iataList(new ArrayList<>(iataCodes)).build();
            Either<ServiceError, List<AirlineAirport>> either = airlineService.batchUpsert(airlineBatchUpsert);
            if (either.isLeft()) {
                LOGGER.error("failed to batch UPSERT airline {} with codes: {}",airline,iataCodes);
            } else {
                LOGGER.info("successful batch UPSERT airline {} with {} records",airline,either.get().size());
            }
            LOGGER.info("-----------------");
        });
    }

    private static void shutdown() {
        executorService.shutdown();
    }

    private static Map<String, Set<String>> fetchRouteMap() {
        switch (flightSyncConfig.getSyncMode()) {
            case FULL_SYNC,AIRLINE_SYNC -> {
                return fetchAirlineRouteMap();
            }
            case RETRY_SYNC -> {
                // load retry routes from target output file
                List<String> failedRouteList = ConstantsDatasync
                        .loadStringListFromDirectFile(flightSyncConfig.getFileName());
                // map origin to destination set
                Map<String,Set<String>> routeMap = new HashMap<>();
                failedRouteList.forEach(item -> {
                    String[] tokens = item.split(":");
                    if (tokens.length != 2) {
                        throw new RuntimeException(String.format("Retry source file %s must be formatted line by " +
                                "line with a valid route, ie 'HNL:HND'",ConstantsDatasync.FAILED_AIRPORT_SCHEDULE_FILE));
                    }
                    Set<String> destinations = routeMap.getOrDefault(tokens[0],new HashSet<>());
                    destinations.add(tokens[1]);
                    routeMap.put(tokens[0],destinations);
                });
                return routeMap;
            }
            default -> throw new RuntimeException(String.format("sync mode %s not yet implemented!",
                flightSyncConfig.getSyncMode().name()));
        }
    }

    private static Map<String, Set<String>> fetchAirlineRouteMap() {
        List<Future<AirlineRouteResult>> airlineFutureList = new ArrayList<>();
        CompletionService<AirlineRouteResult> completionService
                = new ExecutorCompletionService<>(executorService);

        // for each airline, submit call to external service as a future, add to future list
        List<Airline> airlineList = flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC) ?
                flightSyncConfig.getAirlineList() : Arrays.asList(Airline.values());
        for (Airline airline : airlineList) {
            Callable<AirlineRouteResult> airlineRouteTask = () -> {
                LOGGER.trace("extracting airline {} routes from external FlightRadar service",airline.name());
                // map service error to airline failure, route list to airline result w route map
                Either<ServiceError, List<RouteFR>> either = FlightRadarService.extractAirlineRoutes(airline);
                if (either.isLeft()) {
                    throw new RuntimeException(String.format("fetch routes for airline %s failed with error: %s",
                            airline.name(),either.getLeft().getException().getMessage()));
                }
                Map<String,Set<String>> airlineRouteMap = filterAndBuildoutRouteMap(either.get());
                LOGGER.trace("airline {} successfully returned {} routes, built out {} origins for route maps",
                        airline.name(),either.get().size(),airlineRouteMap.size());
                return new AirlineRouteResult(airline,airlineRouteMap);
            };
            airlineFutureList.add(completionService.submit(airlineRouteTask));
        }
        int totalTasks = airlineFutureList.size();
        int completedTasks = 0;

        Map<String,Set<String>> finalRouteMap = new HashMap<>();
        while (completedTasks < totalTasks) {
            try {
                Future<AirlineRouteResult> nextCompletedFuture = completionService.take();
                AirlineRouteResult airlineRouteResult = nextCompletedFuture.get();
                completedTasks++;
                LOGGER.info("Airline progress: {}/{} task {} completed",
                        completedTasks,totalTasks,airlineRouteResult.airline);
                int presize = finalRouteMap.size();
                airlineRouteResult.originToDestinationSet.forEach((origin, destinationSet) -> {
                    finalRouteMap.merge(origin, destinationSet, (oldSet, newSet) -> {
                        oldSet.addAll(newSet);
                        return oldSet;
                    });
                });
                if (finalRouteMap.size() > presize) {
                    LOGGER.debug("merged airline {}, {} additional origin keys",
                            airlineRouteResult.airline,finalRouteMap.size()-presize);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(String.format("exception thrown while fetching airline routes, only " +
                        "%d/%d tasks completed",completedTasks,totalTasks));
            }
        }
        LOGGER.info("************************");
        LOGGER.info("completed {}/{} route tasks with {} origin keys added", completedTasks,totalTasks,
                finalRouteMap.size());
        return finalRouteMap;
    }

    private static Map<String, Set<String>> filterAndBuildoutRouteMap(List<RouteFR> routeFRList) {
        Map<String,Set<String>> originToDestinationMap = new HashMap<>();
        routeFRList.forEach(routeFR -> {
            if (routeFR.getAirport1() == null || routeFR.getAirport2() == null
                    || StringUtils.isBlank(routeFR.getAirport1().getIata())
                    || StringUtils.isBlank(routeFR.getAirport2().getIata())) return;
            String airportCode1 = routeFR.getAirport1().getIata();
            String airportCode2 = routeFR.getAirport2().getIata();
            if (airportCode1.equals(airportCode2)) return;

            if (!voyagerReference.allAirportCodeSet.contains(airportCode1)) {
                Either<ServiceError,Airport> airportEither = buildMissingAirport(airportCode1,routeFR.getAirport1());
                if (airportEither.isLeft()) {
                    voyagerReference.missingAirportMap.put(airportCode1,routeFR.getAirport1());
                    LOGGER.error("failed to build missing airport with error {}, added {} to to voyager reference",
                            airportEither.getLeft().getException().getMessage(),routeFR.getAirport1());
                    return;
                }
                Airport airport = airportEither.get();
                voyagerReference.allAirportCodeSet.add(airport.getIata());
                LOGGER.info("successfully created {} airport {}, added to voyager reference",airport.getType(),airport);
                if (!airport.getType().equals(AirportType.CIVIL)) {
                    LOGGER.info("skipping route {}:{} due to non-CIVIL airport",airportCode1,airportCode2);
                    return;
                }
                voyagerReference.civilAirportCodeSet.add(airport.getIata());
            }


            if (!voyagerReference.allAirportCodeSet.contains(airportCode2)) {
                Either<ServiceError,Airport> airportEither = buildMissingAirport(airportCode2,routeFR.getAirport1());
                if (airportEither.isLeft()) {
                    voyagerReference.missingAirportMap.put(airportCode1,routeFR.getAirport1());
                    LOGGER.error("failed to build missing airport with error {}, added {} to to voyager reference",
                            airportEither.getLeft().getException().getMessage(),routeFR.getAirport1());
                    return;
                }
                Airport airport = airportEither.get();
                voyagerReference.allAirportCodeSet.add(airport.getIata());
                LOGGER.info("successfully created {} airport {}, added to voyager reference",airport.getType(),airport);
                if (!airport.getType().equals(AirportType.CIVIL)) {
                    LOGGER.info("skipping route {}:{} due to non-CIVIL airport",airportCode1,airportCode2);
                    return;
                }
                voyagerReference.civilAirportCodeSet.add(airport.getIata());
            }

            Set<String> destinationSet = originToDestinationMap.getOrDefault(airportCode1,new HashSet<>());
            destinationSet.add(airportCode2);
            originToDestinationMap.put(airportCode1,destinationSet);
        });
        return originToDestinationMap;
    }

    private static Either<ServiceError, Airport> buildMissingAirport(String iata, AirportFR airportFR) {
        String name = airportFR.getName();
        String city = airportFR.getCity();
        Double latitude = airportFR.getLat();
        Double longitude = airportFR.getLon();

        String countryCode = null;
        AirportType airportType = AirportType.UNVERIFIED;
        Either<ServiceError, AirportCH> either = ChAviationService.getAirportCH(iata);
        if (either.isRight()) {
            AirportCH airportCH = either.get();
            countryCode = airportCH.getCountryCode();
            airportType = airportCH.getType();
            if (StringUtils.isBlank(name)) name = airportCH.getName();
            if (latitude == null) latitude = airportCH.getLatitude();
            if (longitude == null) longitude = airportCH.getLongitude();
        }

        if (latitude == null || longitude == null) {
            return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new RuntimeException(
                    String.format("required latitude/longitude missing for %s airport creation with data: %s",
                            iata,airportFR))));
        }

        //    private String subdivision;
        //    private String zoneId;
        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(latitude).longitude(longitude).build();
        Either<ServiceError, List<GeoPlace>> geoNameEither = geoService.findNearbyPlaces(geoNearbyQuery);
        if (geoNameEither.isLeft()) {
            return Either.left(geoNameEither.getLeft());
        }
        if (geoNameEither.get().isEmpty()) {
            return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new RuntimeException(
                    String.format("GeoNames returned no results for lat,lng (%f,%f) for iata: %s with data: %s",
                            latitude,longitude,iata,airportFR))));
        }
        GeoPlace geoPlace = geoNameEither.get().get(0);
        String subdivision = geoPlace.getAdminName1();
        if (StringUtils.isBlank(city)) city = geoPlace.getName();
        if (StringUtils.isBlank(countryCode)) countryCode = geoPlace.getCountryCode();
        GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().latitude(latitude).longitude(longitude).build();
        Either<ServiceError, GeoTimezone> timezoneEither = geoService.getTimezone(geoTimezoneQuery);
        if (timezoneEither.isLeft()) {
            return Either.left(timezoneEither.getLeft());
        }
        String zoneId = timezoneEither.get().getTimezoneId();
        AirportForm airportForm = AirportForm.builder().iata(iata).name(name).city(city).subdivision(subdivision)
                .countryCode(countryCode).airportType(airportType.name()).zoneId(zoneId)
                .latitude(String.valueOf(latitude)).longitude(String.valueOf(longitude)).build();
        return airportService.createAirport(airportForm);
    }

    private static void init(String[] args) {
        flightSyncConfig = new FlightSyncConfig(args);
        LOGGER.info("initializing {} with args: {}",FlightSync.class.getSimpleName(),String.join(" ", flightSyncConfig.toArgs()));
        executorService = Executors.newFixedThreadPool(flightSyncConfig.getThreadCount());
        VoyagerServiceRegistry.initialize(flightSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airlineService = voyagerServiceRegistry.get(AirlineService.class);
        routeService = voyagerServiceRegistry.get(RouteService.class);
        flightService = voyagerServiceRegistry.get(FlightService.class);
        airportService = voyagerServiceRegistry.get(AirportService.class);
        geoService = voyagerServiceRegistry.get(GeoService.class);
        buildoutVoyagerReference();
    }

    private static void buildoutVoyagerReference() {
        // load all airport code set
        Either<ServiceError, List<String>> allAirportCodesEither = airportService.getIATACodes();
        if (allAirportCodesEither.isLeft()) {
            Exception exception = allAirportCodesEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        voyagerReference.allAirportCodeSet.addAll(allAirportCodesEither.get());
        LOGGER.info("voyager reference loaded {} codes in all airport codes set",
                voyagerReference.allAirportCodeSet.size());

        // load civil codes set
        IataQuery iataQuery = IataQuery.builder().withAirportTypeList(List.of(AirportType.CIVIL)).build();
        Either<ServiceError, List<String>> civilCodesEither = airportService.getIATACodes(iataQuery);
        if (civilCodesEither.isLeft()) {
            Exception exception = civilCodesEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        List<String> civilCodes = civilCodesEither.get();
        voyagerReference.civilAirportCodeSet.addAll(civilCodes);
        LOGGER.info("voyager reference loaded {} codes in civil airport codes set",
                voyagerReference.civilAirportCodeSet.size());
    }
}
