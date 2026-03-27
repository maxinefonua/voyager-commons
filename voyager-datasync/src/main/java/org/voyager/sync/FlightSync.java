package org.voyager.sync;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.route.*;
import org.voyager.sdk.service.*;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.commons.error.ServiceError;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.FlightProcessor;
import org.voyager.sync.service.RouteProcessor;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.service.impl.AirportReferenceImpl;
import org.voyager.sync.service.impl.FlightProcessorImpl;
import org.voyager.sync.service.impl.RouteProcessorImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightSync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static RouteSyncService routeSyncService;
    private static RouteProcessor routeProcessor;
    private static AirlineService airlineService;
    private static FlightService flightService;
    private static AirportService airportService;
    private static AirportReference airportReference;
    private static GeoService geoService;
    private static ExecutorService executorService;
    private static FlightProcessor flightProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        init(args);
        removePreRetentionDays(flightSyncConfig.getRetentionDays(),flightSyncConfig.getSyncMode(),flightService);
        boolean isRetry = flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.RETRY_SYNC);
        List<Route> toProcess = routeProcessor.fetchRoutesToProcess(isRetry);
        if (toProcess.isEmpty()) {
            LOGGER.info("confirmed no pending routes to process, exiting");
            shutdown();
            long durationMs = System.currentTimeMillis()-startTime;
            int sec = (int) (durationMs/1000);
            int min = sec/60;
            sec %= 60;
            int hr = min/60;
            min %= 60;
            LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
            return;
        }
        flightProcessor.process(toProcess);
        shutdown();
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
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

    private static void shutdown() {
        executorService.shutdown();
    }

//    private static Map<String, Set<String>> fetchRouteMap() {
//        switch (flightSyncConfig.getSyncMode()) {
//            case FULL_SYNC,AIRLINE_SYNC -> {
//                return fetchAirlineRouteMap();
//            }
//            case RETRY_SYNC -> {
//                // load retry routes from target output file
//                List<String> failedRouteList = ConstantsDatasync
//                        .loadStringListFromDirectFile(flightSyncConfig.getRetryRouteFileName());
//                // map origin to destination set
//                Map<String,Set<String>> routeMap = new HashMap<>();
//                failedRouteList.forEach(item -> {
//                    String[] tokens = item.split(":");
//                    if (tokens.length != 2) {
//                        throw new RuntimeException(String.format("Retry source file %s must be formatted line by " +
//                                "line with a valid route, ie 'HNL:HND'",flightSyncConfig.getRetryRouteFileName()));
//                    }
//                    if (airportReference.isSavedAirport(tokens[0])
//                            && airportReference.getCivilAirportOption(tokens[0]).isEmpty()) {
//                        LOGGER.info("ignoring non-civil airport {} in retry route {}:{}",tokens[0],tokens[0],tokens[1]);
//                        return;
//                    }
//                    if (airportReference.isSavedAirport(tokens[1])
//                            && airportReference.getCivilAirportOption(tokens[1]).isEmpty()) {
//                        LOGGER.info("ignoring non-civil airport {} in retry route {}:{}",tokens[1],tokens[0],tokens[1]);
//                        return;
//                    }
//                    Set<String> destinations = routeMap.getOrDefault(tokens[0],new HashSet<>());
//                    destinations.add(tokens[1]);
//                    routeMap.put(tokens[0],destinations);
//                });
//                return routeMap;
//            }
//            default -> throw new RuntimeException(String.format("sync mode %s not yet implemented!",
//                flightSyncConfig.getSyncMode().name()));
//        }
//    }

//    private static Map<String, Set<String>> fetchAirlineRouteMap() {
//        List<Future<AirlineRouteResult>> airlineFutureList = new ArrayList<>();
//        CompletionService<AirlineRouteResult> completionService
//                = new ExecutorCompletionService<>(executorService);
//
//        // for each airline, submit call to external service as a future, add to future list
//        List<Airline> airlineList = flightSyncConfig.getSyncMode().equals(FlightSyncConfig.SyncMode.AIRLINE_SYNC) ?
//                flightSyncConfig.getAirlineList() : Arrays.asList(Airline.values());
//        for (Airline airline : airlineList) {
//            Callable<AirlineRouteResult> airlineRouteTask = () -> {
//                LOGGER.trace("extracting airline {} routes from external FlightRadar service",airline.name());
//                // map service error to airline failure, route list to airline result w route map
//                Either<ServiceError, List<RouteFR>> either = FlightRadarService.extractAirlineRoutes(airline);
//                if (either.isLeft()) {
//                    throw new RuntimeException(String.format("fetch routes for airline %s failed with error: %s",
//                            airline.name(),either.getLeft().getException().getMessage()));
//                }
//                Map<String,Set<String>> airlineRouteMap = filterAndBuildoutRouteMap(either.get());
//                LOGGER.trace("airline {} successfully returned {} routes, built out {} origins for route maps",
//                        airline.name(),either.get().size(),airlineRouteMap.size());
//                return new AirlineRouteResult(airline,airlineRouteMap);
//            };
//            airlineFutureList.add(completionService.submit(airlineRouteTask));
//        }
//        int totalTasks = airlineFutureList.size();
//        int completedTasks = 0;
//
//        Map<String,Set<String>> finalRouteMap = new HashMap<>();
//        while (completedTasks < totalTasks) {
//            try {
//                Future<AirlineRouteResult> nextCompletedFuture = completionService.take();
//                AirlineRouteResult airlineRouteResult = nextCompletedFuture.get();
//                completedTasks++;
//                int preSize = finalRouteMap.size();
//                airlineRouteResult.originToDestinationSet.forEach((origin, destinationSet) ->
//                        finalRouteMap.merge(origin, destinationSet, (oldSet, newSet) -> {
//                            oldSet.addAll(newSet);
//                            return oldSet;
//                        }));
//                if (finalRouteMap.size() > preSize) {
//                    LOGGER.debug("merged airline {}, {} additional origin keys",
//                            airlineRouteResult.airline,finalRouteMap.size()-preSize);
//                }
//                LOGGER.info("Airline progress: {}/{} task {} completed, route map size at {}",
//                        completedTasks,totalTasks,airlineRouteResult.airline,finalRouteMap.size());
//            } catch (InterruptedException | ExecutionException e) {
//                shutdown();
//                throw new RuntimeException(String.format("exception thrown while fetching airline routes, only " +
//                        "%d/%d tasks completed",completedTasks,totalTasks),e);
//            }
//        }
//        LOGGER.info("************************");
//        LOGGER.info("completed {}/{} route tasks with {} origin keys added", completedTasks,totalTasks,
//                finalRouteMap.size());
//        return finalRouteMap;
//    }

//    private static Map<String, Set<String>> filterAndBuildoutRouteMap(List<RouteFR> routeFRList) {
//        Map<String,Set<String>> originToDestinationMap = new HashMap<>();
//        routeFRList.forEach(routeFR -> {
//            if (routeFR.getAirport1() == null || routeFR.getAirport2() == null
//                    || StringUtils.isBlank(routeFR.getAirport1().getIata())
//                    || StringUtils.isBlank(routeFR.getAirport2().getIata())) return;
//            String airportCode1 = routeFR.getAirport1().getIata();
//            String airportCode2 = routeFR.getAirport2().getIata();
//            if (airportCode1.equals(airportCode2)) return;
//
//            if (!airportReference.isSavedAirport(airportCode1)) {
//                Either<ServiceError,Airport> airportEither = buildMissingAirport(airportCode1,routeFR.getAirport1());
//                if (airportEither.isLeft()) {
//                    airportReference.addMissingAirport(airportCode1,routeFR.getAirport1());
//                    LOGGER.error("failed to build missing airport with error {}, added {} to to voyager reference",
//                            airportEither.getLeft().getException().getMessage(),routeFR.getAirport1());
//                    return;
//                }
//                Airport airport = airportEither.get();
//                if (airport.getType().equals(AirportType.CIVIL)) {
//                    airportReference.addCivilAirport(airport);
//                    LOGGER.info("successfully created {} airport {}, added to airport reference",
//                            airport.getType(),airport);
//                } else {
//                    airportReference.addNonCivilAirport(airport.getIata());
//                    LOGGER.info("successfully created non-CIVIL {} {} to airport reference, but skipping non-CIVIl route {}:{}",
//                            airport.getType(),airport.getIata(),airportCode1,airportCode2);
//                    return;
//                }
//            }
//
//
//            if (!airportReference.isSavedAirport(airportCode2)) {
//                Either<ServiceError,Airport> airportEither = buildMissingAirport(airportCode2,routeFR.getAirport2());
//                if (airportEither.isLeft()) {
//                    airportReference.addMissingAirport(airportCode2,routeFR.getAirport2());
//                    LOGGER.error("failed to build missing airport with error {}, added {} to to airport reference",
//                            airportEither.getLeft().getException().getMessage(),routeFR.getAirport2());
//                    return;
//                }
//                Airport airport = airportEither.get();
//
//                if (airport.getType().equals(AirportType.CIVIL)) {
//                    airportReference.addCivilAirport(airport);
//                    LOGGER.info("successfully created {} airport {}, added to airport reference",
//                            airport.getType(),airport);
//                } else {
//                    airportReference.addNonCivilAirport(airport.getIata());
//                    LOGGER.info("skipping route {}:{} due to non-CIVIL airport {}",
//                            airportCode1,airportCode2, airport.getIata());
//                    return;
//                }
//            }
//
//            Set<String> destinationSet = originToDestinationMap.getOrDefault(airportCode1,new HashSet<>());
//            destinationSet.add(airportCode2);
//            originToDestinationMap.put(airportCode1,destinationSet);
//
//            Set<String> reverseSet = originToDestinationMap.getOrDefault(airportCode2,new HashSet<>());
//            reverseSet.add(airportCode1);
//            originToDestinationMap.put(airportCode2,reverseSet);
//        });
//        return originToDestinationMap;
//    }

//    private static Either<ServiceError, Airport> buildMissingAirport(String iata, AirportFR airportFR) {
//        String name = airportFR.getName();
//        String city = airportFR.getCity();
//        Double latitude = airportFR.getLat();
//        Double longitude = airportFR.getLon();
//
//        String countryCode = null;
//        AirportType airportType = AirportType.UNVERIFIED;
//        Either<ServiceError, AirportCH> either = ChAviationService.getAirportCH(iata);
//        if (either.isRight()) {
//            AirportCH airportCH = either.get();
//            countryCode = airportCH.getCountryCode();
//            airportType = airportCH.getType();
//            if (StringUtils.isBlank(name)) name = airportCH.getName();
//            if (latitude == null) latitude = airportCH.getLatitude();
//            if (longitude == null) longitude = airportCH.getLongitude();
//        }
//
//        if (latitude == null || longitude == null) {
//            return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new RuntimeException(
//                    String.format("required latitude/longitude missing for %s airport creation with data: %s",
//                            iata,airportFR))));
//        }
//        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(latitude).longitude(longitude).build();
//        Either<ServiceError, List<GeoPlace>> geoNameEither = geoService.findNearbyPlaces(geoNearbyQuery);
//        if (geoNameEither.isLeft()) {
//            return Either.left(geoNameEither.getLeft());
//        }
//        if (geoNameEither.get().isEmpty()) {
//            return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new RuntimeException(
//                    String.format("GeoNames returned no results for lat,lng (%f,%f) for iata: %s with data: %s",
//                            latitude,longitude,iata,airportFR))));
//        }
//        GeoPlace geoPlace = geoNameEither.get().get(0);
//        String subdivision = geoPlace.getAdminName1();
//        if (StringUtils.isBlank(city)) city = geoPlace.getName();
//        if (StringUtils.isBlank(countryCode)) countryCode = geoPlace.getCountryCode();
//        GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().latitude(latitude).longitude(longitude).build();
//        Either<ServiceError, GeoTimezone> timezoneEither = geoService.getTimezone(geoTimezoneQuery);
//        if (timezoneEither.isLeft()) {
//            return Either.left(timezoneEither.getLeft());
//        }
//        String zoneId = timezoneEither.get().getTimezoneId();
//        AirportForm airportForm = AirportForm.builder().iata(iata).name(name).city(city).subdivision(subdivision)
//                .countryCode(countryCode).airportType(airportType.name()).zoneId(zoneId)
//                .latitude(String.valueOf(latitude)).longitude(String.valueOf(longitude)).build();
//        return airportService.createAirport(airportForm);
//    }

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
        airportReference = new AirportReferenceImpl(airportService);
        routeProcessor = new RouteProcessorImpl(routeService,routeSyncService,airportReference);
        flightProcessor = new FlightProcessorImpl(
                routeSyncService,flightService,airlineService,airportReference,routeService,flightSyncConfig,
                routeProcessor);
    }
}
