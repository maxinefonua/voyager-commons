package org.voyager.sync;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceException;
import org.voyager.commons.model.flight.FlightUpsert;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.response.PagedResponse;
import org.voyager.commons.model.route.*;
import org.voyager.sdk.model.AirportQuery;
import org.voyager.sdk.service.*;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airport.Airport;
import org.voyager.sync.model.chaviation.AirportCH;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.sync.model.flightradar.AirportFR;
import org.voyager.sync.model.flightradar.RouteFR;
import org.voyager.sync.model.flightradar.search.AirlineFR;
import org.voyager.sync.model.flightradar.search.AirportScheduleFR;
import org.voyager.sync.model.flights.AirlineRouteResult;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.sync.reference.VoyagerReference;
import org.voyager.sync.service.FlightRadarService;
import org.voyager.sync.service.ChAviationService;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.utils.ConstantsDatasync;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public class FlightSync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static RouteSyncService routeSyncService;
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
        Map<String, Route> routeMap = getMappedRoutes();
        removePreRetentionDays(flightSyncConfig.getRetentionDays(),flightSyncConfig.getSyncMode(),flightService);
        List<Route> toProcess = getRoutesToProcess(routeMap);
        processRouteListAndAirlineMap(toProcess);
        shutdown();
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    private static List<Route> getRoutesToProcess(Map<String, Route> routeMap) {
        List<RouteSync> pending = getPendingRouteSyncList();
        if (pending.isEmpty()) {
            if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.RETRY_SYNC)) {
                LOGGER.info("voyager returned empty route sync list with status {}", Status.PENDING);
                return List.of();
            } else {
                LOGGER.info("voyager returned empty route sync list with status {}, setting all routes to {}",
                        Status.PENDING, Status.PENDING);
                Map<String, Set<String>> airlineRouteMap = fetchRouteMap();
                return setConfirmedRoutesToPending(routeMap,airlineRouteMap);
            }
        } else {
            LOGGER.info("voyager returned {} route sync with status {}",
                    pending.size(),Status.PENDING);
            List<Route> toProcess = new ArrayList<>();
            Map<Integer,Route> idToRouteMap = new HashMap<>();
            routeMap.forEach((key,route)-> {
                idToRouteMap.put(route.getId(),route);
            });
            pending.forEach(routeSync -> {
                Route route = idToRouteMap.get(routeSync.getId());
                if (route == null) {
                    route = routeService.getRoute(routeSync.getId()).getOrElse(route);
                    if (route == null) {
                        LOGGER.info("route does not exist for route sync {}", routeSync);
                        return;
                    } else if (!voyagerReference.civilAirportMap.containsKey(route.getOrigin())
                            || !voyagerReference.civilAirportMap.containsKey(route.getDestination()) ) {
                        RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                                .status(Status.COMPLETED)
                                .build();
                        Either<ServiceError, RouteSync> either = routeSyncService
                                .patchRouteSync(route.getId(),routeSyncPatch);
                        if (either.isLeft()) {
                            LOGGER.error("failed to patch route id {} as completed for non-civil route, error: {}",
                                    route.getId(),either.getLeft().getException().getMessage());
                        } else {
                            LOGGER.info("successful patch COMPLETE of route id: {} with non-civil route {}:{}",
                                    route.getId(),route.getOrigin(),route.getDestination());
                        }
                        return;
                    }
                }
                if (route.getOrigin() == null || route.getDestination() == null) {
                    RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                            .status(Status.COMPLETED)
                            .build();
                    Either<ServiceError, RouteSync> either = routeSyncService
                            .patchRouteSync(route.getId(),routeSyncPatch);
                    if (either.isLeft()) {
                        LOGGER.error("failed to patch route id {} as completed, error: {}",
                                route.getId(),either.getLeft().getException().getMessage());
                    } else {
                        LOGGER.info("successful patch COMPLETE of route id: {} with route {}:{}",
                                route.getId(),route.getOrigin(),route.getDestination());
                    }
                    return;
                }
                toProcess.add(route);
            });
            LOGGER.info("processing {} routes total", toProcess.size());
            toProcess.sort(Comparator.comparing(Route::getOrigin).thenComparing(Route::getDestination));
            return toProcess;
        }
    }

    private static List<RouteSync> getPendingRouteSyncList() {
        Either<ServiceError, List<RouteSync>> either = routeSyncService.getByStatus(Status.PENDING);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            LOGGER.error("ServiceError returned from get route sync by status {}, error: {}",
                    Status.PENDING,exception.getMessage());
            shutdown();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        return either.get();
    }

    private static void processRouteListAndAirlineMap(List<Route> routeList) {
        List<Future<Either<AirportScheduleFailure,AirportScheduleResult>>> futureList = new ArrayList<>();
        CompletionService<Either<AirportScheduleFailure,AirportScheduleResult>> completionService =
                new ExecutorCompletionService<>(executorService);

        routeList.forEach((route)->{
            String airportCode1 = route.getOrigin();
            String airportCode2 = route.getDestination();
            Callable<Either<AirportScheduleFailure,AirportScheduleResult>> airportScheduleTask = ()->
                    FlightRadarService.extractAirportResponseWithRetry(airportCode1,airportCode2)
                            .mapLeft(serviceError ->
                                    new AirportScheduleFailure(airportCode1,airportCode2,serviceError))
                            .flatMap(airportScheduleFROption -> {
                                if (airportScheduleFROption.isEmpty()) {
                                    RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                                            .status(Status.COMPLETED)
                                            .build();
                                    Either<ServiceError, RouteSync> patchEither = routeSyncService.patchRouteSync(
                                            route.getId(),routeSyncPatch);
                                    if (patchEither.isLeft()) {
                                        Exception exception = patchEither.getLeft().getException();
                                        LOGGER.error("failed to patch route sync for empty route {}:{}, error: {}",
                                                airportCode1,airportCode2,exception.getMessage());
                                        return left(new AirportScheduleFailure(airportCode1,airportCode2,
                                                patchEither.getLeft()));
                                    }
                                    LOGGER.trace("{}:{} returned no flights, successfully patched as completed", airportCode1, airportCode2);
                                    return right(new AirportScheduleResult(airportCode1, airportCode2,
                                            0, 0, 0, Set.of()));
                                }
                                return processAirportSchedule(airportCode1, airportCode2,
                                        airportScheduleFROption.get(), flightService, routeService);
                            });
            futureList.add(completionService.submit(airportScheduleTask));
        });

        int totalTasks = futureList.size();
        int completedTasks = 0;
        int processingErrors = 0;
        int flightCreates = 0;
        int flightPatches = 0;
        int flightSkips = 0;
        List<AirportScheduleFailure> failureList = new ArrayList<>();
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
                    flightCreates += result.flightsCreated;
                    flightPatches += result.flightsPatched;
                    flightSkips += result.flightsSkipped;
                    LOGGER.info("task {}/{} completed for route {}:{} with {} creates, {} patches, {} skips",
                            completedTasks,totalTasks, result.airportCode1,result.airportCode2,
                            result.flightsCreated,result.flightsPatched,result.flightsSkipped);
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
        printAndSaveErrors(failureList,airlineToIataMap);
        if (failureList.isEmpty() && processingErrors == 0) {
        }
    }

    private static List<Route> setConfirmedRoutesToPending(Map<String, Route> routeMap, Map<String, Set<String>> airlineRouteMap) {
            List<Route> routeList = new ArrayList<>();
            LOGGER.info("loaded {} total routes from voyager", routeMap.size());
            List<Integer> routeIdList = new ArrayList<>();
            airlineRouteMap.forEach((origin, destinationSet) -> {
                destinationSet.forEach((destination) -> {
                    String key = String.format("%s:%s", origin, destination);
                    if (routeMap.containsKey(key)) {
                        Route route = routeMap.get(key);
                        routeIdList.add(route.getId());
                        routeList.add(route);
                    } else {
                        Airport originAirport = voyagerReference.civilAirportMap.get(origin);
                        Airport destinationAirport = voyagerReference.civilAirportMap.get(destination);
                        if (originAirport == null
                                || destinationAirport == null) {
                            LOGGER.info("route from airline route map contains non-civil airport, skipping");
                            return;
                        }
                        Either<ServiceError, Route> either =
                                fetchOrCreateRoute(originAirport,destinationAirport,routeService);
                        if (either.isLeft()) {
                            LOGGER.error("create civil route {}:{} from airline route map failed with error: {}",
                                    origin,destination,either.getLeft().getException().getMessage());
                            shutdown();
                            throw new IllegalStateException(either.getLeft().getException().getMessage());
                        } else {
                            routeMap.put(String.format("%s:%s",origin,destination),either.get());
                            LOGGER.info("successfully created civil route {}:{}",origin,destination);
                        }
                    }
                });
            });
            RouteSyncBatchUpdate routeSyncBatchUpdate = RouteSyncBatchUpdate.builder()
                    .routeIdList(routeIdList)
                    .status(Status.PENDING)
                    .build();
            Either<ServiceError,Integer> updateEither = routeSyncService.batchUpdate(routeSyncBatchUpdate);
            if (updateEither.isLeft()) {
                Exception exception = updateEither.getLeft().getException();
                LOGGER.error("Service Error returned on batch update, error: {}",exception.getMessage());
                shutdown();
                throw new RuntimeException(exception.getMessage(), exception);
            }
            LOGGER.info("set {} route sync entries to {}",updateEither.get(),Status.PENDING.name());
            routeList.sort(Comparator.comparing(Route::getOrigin).thenComparing(Route::getDestination));
            return routeList;
    }

    private static Map<String, Route> getMappedRoutes() {
        Either<ServiceError, List<Route>> either = routeService.getRoutes();
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(exception.getMessage(), exception);
        }
        List<Route> routeList = either.get();
        Map<String, Route> routeMap = new HashMap<>();
        routeList.forEach(route -> {
            if (!voyagerReference.civilAirportMap.containsKey(route.getOrigin()) ||
                    !voyagerReference.civilAirportMap.containsKey(route.getDestination())) {
                LOGGER.trace("skipping non-civil route {}:{}",route.getOrigin(),route.getDestination());
            } else {
                String key = String.format("%s:%s", route.getOrigin(), route.getDestination());
                routeMap.put(key, route);
            }
        });
        LOGGER.info("loaded {} routes from voyager", routeMap.size());
        return routeMap;
    }

    private static void removePreRetentionDays(int retentionDays, FlightSyncConfig.SyncMode syncMode, FlightService flightService) {
        FlightBatchDelete flightBatchDelete = FlightBatchDelete.builder()
                .daysPast(String.valueOf(retentionDays)).build();
        if (syncMode.equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)) {
            flightSyncConfig.getAirlineList().forEach(airline -> {
                flightBatchDelete.setAirline(airline.name());
                Either<ServiceError,Integer> either = flightService.batchDelete(flightBatchDelete);
                if (either.isLeft()) {
                    ServiceError serviceError = either.getLeft();
                    LOGGER.error("batch DELETE for retention days {} with airline {} failed with service error: {}",
                            retentionDays,airline.name(),serviceError.getMessage());
                } else {
                    LOGGER.info("batch DELETE for retention days {} with airline {} successfully deleted {} records",
                            retentionDays,airline.name(),either.get());
                }
            });
        } else {
            Either<ServiceError, Integer> either = flightService.batchDelete(flightBatchDelete);
            if (either.isLeft()) {
                ServiceError serviceError = either.getLeft();
                LOGGER.error("batch DELETE for retention days {} for ALL airlines failed with service error: {}",
                        retentionDays, serviceError.getMessage());
            } else {
                LOGGER.info("batch DELETE for retention days {} for ALL airlines successfully deleted {} records",
                        retentionDays, either.get());
            }
        }
    }

    private static Either<AirportScheduleFailure,AirportScheduleResult> processAirportSchedule(
            String airportCode1, String airportCode2, AirportScheduleFR airportScheduleFR, FlightService flightService,
            RouteService routeService) {
        Set<Airline> airlineSet = new HashSet<>();
        AtomicInteger flightCreates = new AtomicInteger(0);
        AtomicInteger flightSkips = new AtomicInteger(0);
        AtomicInteger flightPatches = new AtomicInteger(0);
        List<FlightUpsert> flightUpsertList = new ArrayList<>();

        Airport airport1 = voyagerReference.civilAirportMap.get(airportCode1);
        Airport airport2 = voyagerReference.civilAirportMap.get(airportCode2);
        if (airport1 == null || airport2 == null) {
            return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,
                    new ServiceError(HttpStatus.NOT_FOUND,
                            new ServiceException("airports not found civil airport map in voyager reference"))));
        }
        Either<ServiceError, Route> processingEither = fetchOrCreateRoute(airport1,airport2,routeService);
        if (processingEither.isLeft()) {
            return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,processingEither.getLeft()));
        }
        Route processingRoute = processingEither.get();
        if (airportScheduleFR.getArrivals() != null && !airportScheduleFR.getArrivals().isEmpty()) {
            Integer routeId = processingRoute.getId();
            airportScheduleFR.getArrivals().forEach((countryName, countryFR) ->
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

        if (airportScheduleFR.getDepartures() != null && !airportScheduleFR.getDepartures().isEmpty()) {
            Either<ServiceError, Route> either = fetchOrCreateRoute(airport2,airport1,routeService);
            if (either.isLeft()) {
                return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,either.getLeft()));
            }
            Integer routeId = either.get().getId();
            airportScheduleFR.getDepartures().forEach((countryName, countryFR) ->
                    countryFR.getIataToFlightsMap().get(airportCode2).getFlightNumberToPlannedMap()
                            .forEach((flightNumber, plannedFR) -> {
                                AirlineFR airlineFR = plannedFR.getAirline();
                                try {
                                    Airline airline = Airline.fromPathVariableFR(airlineFR.getUrl());
                                    if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)
                                            && !flightSyncConfig.getAirlineList().contains(airline)) {
                                        LOGGER.info("in {} mode, skipping flight of excluded airline {}",
                                                flightSyncConfig.getSyncMode().name(), airline.name());
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
            Either<ServiceError,FlightBatchUpsertResult> either = flightService.batchUpsert(
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
        RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                .status(Status.COMPLETED)
                .build();
        Either<ServiceError, RouteSync> patchEither = routeSyncService.patchRouteSync(
                processingRoute.getId(), routeSyncPatch);
        if (patchEither.isLeft()) {
            Exception exception = patchEither.getLeft().getException();
            LOGGER.error("failed to patch completed sync for route {}, error: {}",processingRoute,exception.getMessage());
            return Either.left(new AirportScheduleFailure(airportCode1,airportCode2,patchEither.getLeft()));
        } else {
            LOGGER.trace("successfully patched as completed for route {}:{}",airportCode1,airportCode2);
        }
        return Either.right(new AirportScheduleResult(airportCode1,airportCode2,flightCreates.get(),flightPatches.get(),
                flightSkips.get(),airlineSet));
    }

    private static Either<ServiceError, Route> fetchOrCreateRoute(Airport originAirport,
                                                                  Airport destinationAirport,
                                                                  RouteService routeService) {
        Either<ServiceError, Route> either = routeService.getRoute(
                originAirport.getIata(),destinationAirport.getIata());
        if (either.isRight()) return either;

        ServiceError serviceError = either.getLeft();
        if (!serviceError.getHttpStatus().equals(HttpStatus.NOT_FOUND)) return either;

        Double distanceKM = Airport.calculateDistanceKm(originAirport.getLatitude(),originAirport.getLongitude(),
                destinationAirport.getLatitude(),destinationAirport.getLongitude());

        RouteForm routeForm = RouteForm.builder().origin(originAirport.getIata())
                .destination(destinationAirport.getIata()).distanceKm(distanceKM).build();
        return routeService.createRoute(routeForm).map(route -> {
            LOGGER.info("successfully created route: {}",route);
            return route;
        });
    }

    private static void printAndSaveErrors(List<AirportScheduleFailure> failureList, Map<Airline, Set<String>> airlineToIataMap) {
        Set<String> failedRoutes = new HashSet<>();
        failureList.forEach(airportScheduleFailure -> {
            LOGGER.error("{}:{} failed with error: {}",airportScheduleFailure.airportCode1,
                    airportScheduleFailure.airportCode2,
                    airportScheduleFailure.serviceError.getException().getMessage());
            failedRoutes.add(String.format("%s:%s",
                    airportScheduleFailure.airportCode1,airportScheduleFailure.airportCode2));
        });
        if (failedRoutes.isEmpty()) {
            if (flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC)) {
                for (Airline airline : flightSyncConfig.getAirlineList()) {
                    Either<ServiceError, Integer> either = flightService.batchDelete(FlightBatchDelete.builder().airline(airline.name())
                            .isActive("false").build());
                    if (either.isLeft()) {
                        LOGGER.error("batch DELETE airline {} flights failed with error: {}",airline.name(),
                                either.getLeft().getException().getMessage());
                    } else {
                        LOGGER.info("successful batch DELETE airline {} flights with {} records",airline.name(),either.get());
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
            processAirlineMap(airlineToIataMap);
        }
    }


    private static void processAirlineMap(Map<Airline, Set<String>> airlineMap) {
        LOGGER.info("processing upsert airline map of {} total airlines", airlineMap.size());
        Map<Airline,List<String>> failedAirlineAirports = new HashMap<>();
        airlineMap.forEach((airline,iataCodes) ->{
            LOGGER.info("upserting airline {} with {} airport codes",
                    airline.name(),iataCodes.size());
            AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().airline(airline.name())
                    .isActive(true).iataList(new ArrayList<>(iataCodes)).build();
            Either<ServiceError, List<AirlineAirport>> either = airlineService.batchUpsert(airlineBatchUpsert);
            if (either.isLeft()) {
                LOGGER.error("failed to batch UPSERT airline {} with codes: {}, error: {}",
                        airline,iataCodes,either.getLeft().getException().getMessage());
                failedAirlineAirports.put(airline,new ArrayList<>(iataCodes));
            } else {
                LOGGER.info("successful batch UPSERT airline {} with {} records",airline,either.get().size());
            }
            LOGGER.info("-----------------");
        });
        ConstantsDatasync.writeAirlineListToFile(failedAirlineAirports,flightSyncConfig.getRetryAirlineFileWriter());
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
                        .loadStringListFromDirectFile(flightSyncConfig.getRetryRouteFileName());
                // map origin to destination set
                Map<String,Set<String>> routeMap = new HashMap<>();
                failedRouteList.forEach(item -> {
                    String[] tokens = item.split(":");
                    if (tokens.length != 2) {
                        throw new RuntimeException(String.format("Retry source file %s must be formatted line by " +
                                "line with a valid route, ie 'HNL:HND'",flightSyncConfig.getRetryRouteFileName()));
                    }
                    if (voyagerReference.allAirportCodeSet.contains(tokens[0])
                            && !voyagerReference.civilAirportMap.containsKey(tokens[0])) {
                        LOGGER.info("ignoring non-civil airport {} in retry route {}:{}",tokens[0],tokens[0],tokens[1]);
                        return;
                    }
                    if (voyagerReference.allAirportCodeSet.contains(tokens[1])
                            && !voyagerReference.civilAirportMap.containsKey(tokens[1])) {
                        LOGGER.info("ignoring non-civil airport {} in retry route {}:{}",tokens[1],tokens[0],tokens[1]);
                        return;
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
                int preSize = finalRouteMap.size();
                airlineRouteResult.originToDestinationSet.forEach((origin, destinationSet) ->
                        finalRouteMap.merge(origin, destinationSet, (oldSet, newSet) -> {
                            oldSet.addAll(newSet);
                            return oldSet;
                        }));
                if (finalRouteMap.size() > preSize) {
                    LOGGER.debug("merged airline {}, {} additional origin keys",
                            airlineRouteResult.airline,finalRouteMap.size()-preSize);
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
                voyagerReference.civilAirportMap.put(airport.getIata(),airport);
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
                voyagerReference.civilAirportMap.put(airport.getIata(),airport);
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
        routeSyncService = voyagerServiceRegistry.get(RouteSyncService.class);
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
        AirportQuery airportQuery = AirportQuery.builder().airportTypeList(List.of(AirportType.CIVIL)).size(1000).page(0).build();
        Either<ServiceError, PagedResponse<Airport>> civilAirportsEither = airportService.getAirports(airportQuery);
        while (civilAirportsEither.isRight()) {
            PagedResponse<Airport> pagedResponse = civilAirportsEither.get();
            pagedResponse.getContent().forEach(airport ->
                    voyagerReference.civilAirportMap.put(airport.getIata(),airport));
            if (pagedResponse.isLast()) {
                LOGGER.info("voyager reference loaded {} codes in civil airport codes set",
                        voyagerReference.civilAirportMap.size());
                return;
            }
            airportQuery.setPage(airportQuery.getPage()+1);
            civilAirportsEither = airportService.getAirports(airportQuery);
        }
        Exception exception = civilAirportsEither.getLeft().getException();
        throw new RuntimeException(exception.getMessage(),exception);
    }
}
