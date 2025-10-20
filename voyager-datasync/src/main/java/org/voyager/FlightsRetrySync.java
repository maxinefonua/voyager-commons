package org.voyager;

import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.FlightSyncConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.airline.Airline;
import org.voyager.model.airline.AirlineAirport;
import org.voyager.model.airline.AirlineBatchUpsert;
import org.voyager.model.airport.Airport;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.flightRadar.search.*;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.reference.AirportsReference;
import org.voyager.service.*;
import org.voyager.service.impl.VoyagerServiceRegistry;
import org.voyager.utils.ConstantsDatasync;
import org.voyager.model.flights.FlightSyncTasks.TaskResult;
import org.voyager.model.flights.FlightSyncTasks.TaskFailure;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlightsRetrySync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static AirlineService airlineService;
    private static FlightService flightService;
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightsRetrySync.class);
    private static final AirportsReference airportsReference = new AirportsReference();

    @ToString
    private static class RouteTask {
        String origin;
        String destination;
        RouteTask(String origin, String destination) {
            this.origin = origin;
            this.destination = destination;
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("printing from routes sync main");
        init(args);
        List<RouteTask> routeTaskList = loadFromFailedRoutesFile();
        runRetryWithFutures(routeTaskList);
        long seconds = (System.currentTimeMillis()-startTime)/1000;
        int minutes = (int) (seconds/60);
        int hours = (int) (minutes/60);
        minutes %= 60;
        seconds %= 60;
        LOGGER.info("completed job in {}hr(s) {}mn and {}sec",hours,minutes,seconds);
    }

    private static void runRetryWithFutures(List<RouteTask> routeTaskList) {
        // Use Atomic variables for thread-safe counting
        AtomicInteger totalFlightCreates = new AtomicInteger(0);
        AtomicInteger totalFlightPatches = new AtomicInteger(0);
        AtomicInteger totalFlightSkips = new AtomicInteger(0);

        // Use concurrent collections
        Map<Airline,Set<String>> airlineToAirportCodes = new ConcurrentHashMap<>();
        CompletionService<Either<TaskFailure,TaskResult>> completionService = new ExecutorCompletionService<>(executorService);

        // Submit all tasks
        List<Future<Either<TaskFailure,TaskResult>>> futures = new ArrayList<>();
        routeTaskList.forEach(routeTask -> {
            Callable<Either<TaskFailure,TaskResult>> task = ()-> fetchAirportScheduleRetryTask(routeTask.origin,routeTask.destination,airlineToAirportCodes,
                    totalFlightCreates,totalFlightSkips,totalFlightPatches);
            futures.add(completionService.submit(task));
        });

        // Process as they complete - FIXED VERSION
        int completed = 0;
        int totalTasks = futures.size();
        int processingErrors = 0;
        int flightsProcessed = 0;

        LOGGER.info("Starting to process {} total route tasks", totalTasks);
        List<TaskFailure> failedRoutes = new ArrayList<>();
        List<TaskResult> flightNumberErrors = new ArrayList<>();
        while (completed < totalTasks) {
            try {
                Future<Either<TaskFailure,TaskResult>> future = completionService.take();
                Either<TaskFailure,TaskResult> either = future.get();
                completed++;
                if (either.isLeft()) {
                    TaskFailure taskFailure = either.getLeft();
                    failedRoutes.add(taskFailure);
                    LOGGER.error("Task {}/{} failed, route {}:{} with error: {}",
                            completed, totalTasks,
                            taskFailure.getOrigin(), taskFailure.getDestination(),
                            taskFailure.getServiceError().getException().getMessage());
                } else {
                    TaskResult taskResult = either.get();
                    int localFlightErrors = taskResult.getFlightNumberErrors() == null ?
                            0 : taskResult.getFlightNumberErrors().size();
                    LOGGER.info("Progress: task {}/{}, route {}:{} - {} flights processed, {} flight errors",
                            completed,totalTasks,taskResult.getOrigin(),taskResult.getDestination(),
                            taskResult.getFlightsProcessed(),localFlightErrors);
                    flightsProcessed += taskResult.getFlightsProcessed();
                    if (localFlightErrors > 0) {
                        flightNumberErrors.add(taskResult);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                processingErrors++;
                completed++;
                LOGGER.error("processing future error for task {}/{}, error: {}",completed,totalTasks,e.getMessage());
            }
        }
        LOGGER.info("*************************************");
        LOGGER.info("Completed processing all {} tasks with {} flights processed, {} creates, {} skips, {} patches, {} failed routes, {} processing errors. Shutting down executor...",
                totalTasks,flightsProcessed,totalFlightCreates.get(),totalFlightSkips.get(),totalFlightPatches.get(),
                failedRoutes.size(),processingErrors);
        executorService.shutdown();
        printRetryResults(airlineToAirportCodes,failedRoutes,flightNumberErrors);
    }

    private static void printRetryResults(Map<Airline, Set<String>> airlineToAirportCodes, List<TaskFailure> failedRoutes, List<TaskResult> flightNumberErrors) {
        airlineToAirportCodes.forEach((airline,airlineAirports) -> {
            LOGGER.info("calling airline UPSERT for {} with {} airports",airline.name(),airlineAirports.size());
            AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().airline(airline.name())
                    .iataList(new ArrayList<>(airlineAirports)).isActive(true).build();
            Either<ServiceError, List<AirlineAirport>> either = airlineService.batchUpsert(airlineBatchUpsert);
            if (either.isLeft()) {
                LOGGER.info("failed to upsert {} records for {}",airlineAirports.size(),airline.name());
            } else {
                LOGGER.info("successful upsert of {} airports for {}",either.get().size(),airline.name());
            }
        });
        LOGGER.info("*************************************");

        failedRoutes.forEach(taskFailure -> LOGGER.info("{}:{} failed with error: {}",
                        taskFailure.getOrigin(),taskFailure.getDestination(),
                taskFailure.getServiceError().getMessage()));

        flightNumberErrors.forEach(taskResult -> {LOGGER.info("{}:{} had {} errors on flightNumbers: {}",
                taskResult.getOrigin(),taskResult.getDestination(),taskResult.getFlightNumberErrors().size(),
                taskResult.getFlightNumberErrors());
        });
    }

    private static List<RouteTask> loadFromFailedRoutesFile() {
        List<String> routeStringList = ConstantsDatasync.loadStringListFromListFile(ConstantsDatasync.FAILED_AIRPORT_SCHEDULE_FILE);
        List<RouteTask> routeTaskList = new ArrayList<>();
        routeStringList.forEach(routeKey -> {
            String[] tokens = routeKey.split(":");
            routeTaskList.add(new RouteTask(tokens[0],tokens[1]));
        });

        LOGGER.info("routes tasks loaded: ");
        routeTaskList.forEach(routeTask -> {
            LOGGER.info(routeTask.toString());
        });
        return routeTaskList;
    }

    private static void init(String[] args) {
        flightSyncConfig = new FlightSyncConfig(args);
        LOGGER.info("initializing FlightSync with args: {}",String.join(" ", flightSyncConfig.toArgs()));
        executorService = Executors.newFixedThreadPool(flightSyncConfig.getThreadCount());
        VoyagerServiceRegistry.initialize(flightSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airlineService = voyagerServiceRegistry.get(AirlineService.class);
        routeService = voyagerServiceRegistry.get(RouteService.class);
        flightService = voyagerServiceRegistry.get(FlightService.class);
        airportService = voyagerServiceRegistry.get(AirportService.class);
        GeoNamesService.initialize(flightSyncConfig.getGNUsername());
    }

    private static Either<TaskFailure,TaskResult> fetchAirportScheduleRetryTask(String airportCode1, String airportCode2,
                                                                                Map<Airline, Set<String>> airlineToAirportCodes,
                                                                                AtomicInteger totalFlightCreates,
                                                                                AtomicInteger totalFlightSkips,
                                                                                AtomicInteger totalFlightPatches) {
        try {
            Either<ServiceError, Option<AirportScheduleFR>> result =
                    FlightRadarService.extractAirportResponseWithRetry(airportCode1, airportCode2);
            if (result.isLeft()) {
                return Either.left(new TaskFailure(airportCode1,airportCode2,result.getLeft()));
            }
            Option<AirportScheduleFR> airportScheduleOption = result.get();

            if (airportScheduleOption.isEmpty()) {
                LOGGER.info("{}:{} returned no flights", airportCode1,airportCode2);
                return Either.right(new TaskResult(airportCode1,airportCode2,0,null));
            }

            // Create thread-local counters for this task
            int[] localCreates = new int[]{0};
            int[] localPatches = new int[]{0};
            int[] localSkips = new int[]{0};
            Map<Airline, Set<String>> localAirlineToAirports = new HashMap<>();
            Set<String> flightErrorNumbers = new HashSet<>();

            int processedFlights = processAirportScheduleRetryMapped(airportCode1,airportCode2,airportScheduleOption.get(),
                    localCreates, localPatches, localSkips, localAirlineToAirports,flightErrorNumbers);

            // Merge local results back to shared state
            totalFlightCreates.addAndGet(localCreates[0]);
            totalFlightPatches.addAndGet(localPatches[0]);
            totalFlightSkips.addAndGet(localSkips[0]);
            localAirlineToAirports.forEach((airline, airports) ->
                    airlineToAirportCodes.merge(airline, airports, (oldSet, newSet) -> {
                        oldSet.addAll(newSet);
                        return oldSet;
                    })
            );
            return Either.right(new TaskResult(airportCode1,airportCode2,processedFlights,flightErrorNumbers.isEmpty() ? null : new ArrayList<>(flightErrorNumbers)));
        } catch (Exception e) {
            LOGGER.error("Unexpected error processing {}:{}, error: {}", airportCode1,airportCode2, e.getMessage());
            return Either.left(new TaskFailure(airportCode1,airportCode2,
                    new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, e)));
        }
    }

    private static int processAirportScheduleRetryMapped(String airportCode1,
                                                         String airportCode2,
                                                         AirportScheduleFR airportScheduleFR,
                                                         int[] localCreates,
                                                         int[] localPatches,
                                                         int[] localSkips,
                                                         Map<Airline, Set<String>> airlineToAirportCodes,
                                                         Set<String> flightErrorNumbers) {
        if (airportScheduleFR.getArrivals() != null && !airportScheduleFR.getArrivals().isEmpty()) {
            airportScheduleFR.getArrivals().forEach((countryName,countryFR)-> {
                if (countryFR.getIataToFlightsMap() == null || countryFR.getIataToFlightsMap().isEmpty()) return;
                processCountryFR(airportCode1,countryName,countryFR,flightErrorNumbers,false,
                        localCreates,localSkips,localPatches,airlineToAirportCodes);
            });
        } else {
            LOGGER.info("no arrivals at {}:{}",airportCode1,airportCode2);
        }

        if (airportScheduleFR.getDepartures() != null && !airportScheduleFR.getDepartures().isEmpty()) {
            airportScheduleFR.getDepartures().forEach((countryName,countryFR)-> {
                if (countryFR.getIataToFlightsMap() == null || countryFR.getIataToFlightsMap().isEmpty()) return;
                processCountryFR(airportCode1,countryName,countryFR,flightErrorNumbers,true,
                        localCreates,localSkips,localPatches, airlineToAirportCodes);
            });
        } else {
            LOGGER.info("no departures from {}:{}",airportCode1,airportCode2);
        }
        return localCreates[0]+localPatches[0]+localSkips[0];
    }

    private static void processCountryFR(String processingAirportCode, String countryName,
                                         CountryFR countryFR, Set<String> flightErrorNumbers, boolean isOrigin,
                                         int[] localCreates, int[] localSkips,
                                         int[] localPatches,
                                         Map<Airline, Set<String>> airlineToAirportCodes) {
        countryFR.getIataToFlightsMap().forEach((iata,airportFlightFR)-> {
            if (iata.equals(processingAirportCode)) {
                LOGGER.error("something went wrong, {} countryFR contains processing airport {}",
                        countryName,processingAirportCode);
                return;
            }
            airportFlightFR.getFlightNumberToPlannedMap().forEach((flightNumber,plannedFR)->{
                AirlineFR airlineFR = plannedFR.getAirline();
                if (airlineFR == null) return;
                String airlineIata = airlineFR.getIata();
                if (StringUtils.isBlank(airlineIata)) return;
                String url = airlineFR.getUrl();
                if (StringUtils.isBlank(airlineIata)) return;
                try {
                    Airline airline = Airline.fromPathVariableFR(url);
                    LOGGER.trace("processing {} flight",airline.name());
                    String origin = isOrigin ? processingAirportCode : iata;
                    String destination = isOrigin ? iata : processingAirportCode;
                    Map<Airline,Set<String>> localAirlineAirportMap = new HashMap<>();
                    Either<String,Integer> flightEither = buildAndSaveFlight(plannedFR,origin,destination,
                            flightNumber,airline,isOrigin,localAirlineAirportMap);
                    localAirlineAirportMap.forEach((airlineKey, airports) ->
                            airlineToAirportCodes.merge(airlineKey, airports, (oldSet, newSet) -> {
                                oldSet.addAll(newSet);
                                return oldSet;
                            })
                    );
                    if (flightEither.isLeft()) {
                        LOGGER.error("adding flight number {} to error flight numbers",flightNumber);
                        flightErrorNumbers.add(flightNumber);
                        return;
                    }

                    int value = flightEither.get();
                    if (value > 0) {
                        localCreates[0]++;
                    } else {
                        if (value < 0) localPatches[0]++;
                        else localSkips[0]++;
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.trace("ignoring unmapped airline {} during route {}:{}, url '{}'",
                            airlineFR.getName(),processingAirportCode,iata,airlineFR.getUrl());
                }
            });
        });
    }

    private static Either<String, Integer> buildAndSaveFlight(PlannedFR plannedFR, String origin, String destination,
                                                              String flightNumber, Airline airline, boolean isOrigin,
                                                              Map<Airline, Set<String>> airlineAirportCodes) {
        Integer routeId = getRouteId(origin,destination,flightNumber);
        if (routeId == null) {
            return Either.left(flightNumber);
        }

        String latestDate = plannedFR.getDateToTimeMap().keySet()
                .stream().max(Comparator.naturalOrder()).orElse(null);
        if (StringUtils.isBlank(latestDate)) {
            LOGGER.error("no latest date for flight {} with origin {}, plannedFR: {}", flightNumber,origin,plannedFR);
            return Either.left(flightNumber);
        }

        AtomicInteger createValue = new AtomicInteger(0);
        Either<String, Flight> flightEither = fetchOrCreateFlight(routeId,flightNumber,airline,
                plannedFR.getDateToTimeMap().get(latestDate),
                createValue,isOrigin,airlineAirportCodes,origin,destination);
        if (flightEither.isLeft()) return Either.left(flightEither.getLeft());
        return Either.right(createValue.get());
    }

    private static Either<String, Flight> fetchOrCreateFlight(Integer routeId, String flightNumber,
                                                              Airline airline, FlightTimeFR flightTimeFR,
                                                              AtomicInteger createValue, boolean isOrigin,
                                                              Map<Airline, Set<String>> airlineAirportCodeMap,
                                                              String origin, String destination) {
        Long departureTimestamp = null;
        Long departureOffset = null;
        Long arrivalTimestamp = null;
        Long arrivalOffset = null;

        if (isOrigin) {
            departureTimestamp = flightTimeFR.getTimestamp();
            departureOffset = flightTimeFR.getOffset();
        } else {
            arrivalTimestamp = flightTimeFR.getTimestamp();
            arrivalOffset = flightTimeFR.getOffset();
        }
        Either<ServiceError, Flight> either = flightService.getFlight(routeId,flightNumber);
        if (either.isLeft()) { // create flight
            FlightForm flightForm = FlightForm.builder()
                    .flightNumber(flightNumber)
                    .routeId(routeId)
                    .isActive(true)
                    .departureTimestamp(departureTimestamp)
                    .departureOffset(departureOffset)
                    .arrivalTimestamp(arrivalTimestamp)
                    .arrivalOffset(arrivalOffset)
                    .airline(airline.name())
                    .build();
            Either<ServiceError,Flight> created = flightService.createFlight(flightForm);
            if (created.isLeft()) {
                Exception exception = created.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                return Either.left(flightNumber);
            }
            LOGGER.info("successfully created flight: " + created.get().toString());
            createValue.set(1);
            return Either.right(created.get());
        }

        // check if patch needed
        Flight existing = either.get();
        boolean isActive = (existing.getZonedDateTimeDeparture() != null && existing.getZonedDateTimeArrival() != null)
                || (existing.getZonedDateTimeArrival() != null && departureTimestamp != null)
                || (arrivalTimestamp != null && existing.getZonedDateTimeDeparture() != null);
        if (!existing.getIsActive().equals(isActive)
                || (existing.getZonedDateTimeDeparture() == null && departureTimestamp != null)
                || (existing.getZonedDateTimeArrival() == null && arrivalTimestamp != null)) {
            FlightPatch flightPatch = FlightPatch.builder()
                    .departureTimestamp(departureTimestamp)
                    .departureOffset(departureOffset)
                    .arrivalTimestamp(arrivalTimestamp)
                    .arrivalOffset(arrivalOffset)
                    .isActive(isActive)
                    .build();
            Either<ServiceError, Flight> patchEither = flightService.patchFlight(existing.getId(), flightPatch);
            if (patchEither.isLeft()) {
                Exception exception = patchEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                return Either.left(flightNumber);
            }
            Flight patched = patchEither.get();
            LOGGER.info("successfully patched flight: {}",patched.toString());
            if (isActive) {
                Set<String> airlineAirportCodes = airlineAirportCodeMap.getOrDefault(airline,new HashSet<>());
                airlineAirportCodes.add(origin);
                airlineAirportCodes.add(destination);
                airlineAirportCodeMap.put(airline,airlineAirportCodes);
            }
            createValue.set(-1);
            return Either.right(patched);
        }
        LOGGER.trace("skipping flight patch with no new data: {}",existing);
        if (existing.getIsActive()) {
            Set<String> airlineAirportCodes = airlineAirportCodeMap.getOrDefault(airline,new HashSet<>());
            airlineAirportCodes.add(origin);
            airlineAirportCodes.add(destination);
            airlineAirportCodeMap.put(airline,airlineAirportCodes);
        }
        return Either.right(existing);
    }

    private static Integer getRouteId(String origin, String destination, String flightNumber) {
        Either<ServiceError, Route> routeEither = fetchOrCreateRoute(origin,destination);
        if (routeEither.isLeft()) {
            Exception exception = routeEither.getLeft().getException();
            LOGGER.error("error fetch/create route with flightNumber {}, route {}:{}, error: {}",
                    flightNumber,origin,destination,exception.getMessage());
            return null;
        }
        return routeEither.get().getId();
    }

    private static Either<ServiceError,Route> fetchOrCreateRoute(String origin,
                                                                 String destination) {
        Either<ServiceError, Route> either = routeService.getRoute(origin, destination);
        if (either.isRight()) return either;
        // create route
        Airport startAirport = airportsReference.getAirportCodeMap().get(origin);
        if (startAirport == null) {
            airportsReference.getMissingAirportFromRoute().add(origin);
            return Either.left(new ServiceError(HttpStatus.NOT_FOUND,new ServiceException(
                    String.format("%s airport not found during route creation, added to missing airports",origin))));
        }

        Airport endAirport = airportsReference.getAirportCodeMap().get(destination);
        if (endAirport == null) {
            airportsReference.getMissingAirportFromRoute().add(destination);
            return Either.left(new ServiceError(HttpStatus.NOT_FOUND,new ServiceException(
                    String.format("%s airport not found during route creation, added to missing airports",destination))));
        }
        Double distanceKm = Airport.calculateDistanceKm(startAirport.getLatitude(),startAirport.getLongitude(),
                endAirport.getLatitude(),endAirport.getLongitude());
        RouteForm routeForm = RouteForm.builder()
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .build();
        return routeService.createRoute(routeForm);
    }
}
