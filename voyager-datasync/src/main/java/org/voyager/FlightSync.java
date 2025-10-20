package org.voyager;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.FlightSyncConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.AirportQuery;
import org.voyager.model.IataQuery;
import org.voyager.model.airline.Airline;
import org.voyager.model.airline.AirlineAirport;
import org.voyager.model.airline.AirlineBatchUpsert;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportCH;
import org.voyager.model.airport.AirportType;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.flightRadar.RouteFR;
import org.voyager.model.flightRadar.airport.AirportDetailsFR;
import org.voyager.model.flightRadar.airport.DetailsFR;
import org.voyager.model.flightRadar.airport.PositionPR;
import org.voyager.model.flightRadar.search.*;
import org.voyager.model.geoname.GeoName;
import org.voyager.model.geoname.Timezone;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.reference.AirportsReference;
import org.voyager.service.AirlineService;
import org.voyager.service.AirportService;
import org.voyager.service.FlightService;
import org.voyager.service.RouteService;
import org.voyager.service.ChAviationService;
import org.voyager.service.FlightRadarService;
import org.voyager.service.GeoNamesService;
import org.voyager.service.impl.VoyagerServiceRegistry;
import org.voyager.utils.ConstantsDatasync;
import org.voyager.model.flights.FlightSyncTasks.TaskResult;
import org.voyager.model.flights.FlightSyncTasks.TaskFailure;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.voyager.utils.ConstantsDatasync.MISSING_AIRPORTS_FILE;
import static org.voyager.utils.ConstantsDatasync.NON_CIVIL_AIRPORTS_FILE;

public class FlightSync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static AirlineService airlineService;
    private static FlightService flightService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);
    private static final AirportsReference airportsReference = new AirportsReference();

    private static class AirlineResult {
        Airline airline;
        Integer routeCount;
        AirlineResult(Airline airline, Integer routeCount){
            this.airline = airline;
            this.routeCount = routeCount;
        }
    }

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        LOGGER.info("printing from routes sync main");
        init(args);
        Map<String,Set<String>> originToDestinations = fetchAirportToAirportMapWithFutures();
        runWithFutures(originToDestinations);
        long seconds = (System.currentTimeMillis()-startTime)/1000;
        int minutes = (int) (seconds/60);
        int hours = (int) (minutes/60);
        minutes %= 60;
        seconds %= 60;
        LOGGER.info("completed job in {}hr(s) {}mn and {}sec",hours,minutes,seconds);
    }

    public static void init(String[] args) {
        flightSyncConfig = new FlightSyncConfig(args);
        LOGGER.info("initializing FlightSync with args: {}",String.join(" ", flightSyncConfig.toArgs()));
        executorService = Executors.newFixedThreadPool(flightSyncConfig.getThreadCount());
        VoyagerServiceRegistry.initialize(flightSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airlineService = voyagerServiceRegistry.get(AirlineService.class);
        routeService = voyagerServiceRegistry.get(RouteService.class);
        flightService = voyagerServiceRegistry.get(FlightService.class);
        AirportService airportService = voyagerServiceRegistry.get(AirportService.class);
        GeoNamesService.initialize(flightSyncConfig.getGNUsername());
        buildoutAirportReference(airportService);
    }

    private static int processAirportScheduleMapped(String airportCode1,
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

    public static void runWithFutures(Map<String,Set<String>> originToDestinationMap) {
        // Use Atomic variables for thread-safe counting
        AtomicInteger totalFlightCreates = new AtomicInteger(0);
        AtomicInteger totalFlightPatches = new AtomicInteger(0);
        AtomicInteger totalFlightSkips = new AtomicInteger(0);

        // Use concurrent collections
        Map<Airline,Set<String>> airlineToAirportCodes = new ConcurrentHashMap<>();
        CompletionService<Either<TaskFailure,TaskResult>> completionService = new ExecutorCompletionService<>(executorService);

        // Submit all tasks
        List<Future<Either<TaskFailure,TaskResult>>> futures = new ArrayList<>();
        originToDestinationMap.forEach((airportCode1, destinationSet) -> {
            destinationSet.forEach(airportCode2 -> {
                Callable<Either<TaskFailure,TaskResult>> task = ()->
                        fetchAirportScheduleTask(airportCode1,airportCode2,airlineToAirportCodes,
                                totalFlightCreates,totalFlightSkips,totalFlightPatches);
                futures.add(completionService.submit(task));
            });
        });

        // Process as they complete - FIXED VERSION
        int completed = 0;
        int totalTasks = futures.size();
        int processingErrors = 0;
        int flightsProcessed = 0;

        LOGGER.info("Starting to process {} total route tasks", totalTasks);
        List<TaskFailure> failedRoutes = new ArrayList<>();
        Set<String> processedAirports = new HashSet<>();
        List<TaskResult> flightNumberErrors = new ArrayList<>();

        while (completed < totalTasks) {
            try {
                Future<Either<TaskFailure,TaskResult>> future = completionService.take();
                Either<TaskFailure,TaskResult> either = future.get();
                completed++;
                if (either.isLeft()) {
                    TaskFailure taskFailure = either.getLeft();
                    processedAirports.add(taskFailure.getOrigin());
                    failedRoutes.add(taskFailure);
                    LOGGER.error("Task {}/{} failed while processing {}/{} airport route {}:{} with error: {}",
                            completed, totalTasks, processedAirports.size(),originToDestinationMap.size(),
                            taskFailure.getOrigin(), taskFailure.getDestination(),
                            taskFailure.getServiceError().getException().getMessage());
                } else {
                    TaskResult taskResult = either.get();
                    int processedSize = processedAirports.size();
                    processedAirports.add(taskResult.getOrigin());
                    if (processedSize < processedAirports.size()) {
                        LOGGER.info("beginning {} airport, {}/{} total airports",taskResult.getOrigin(),
                                processedAirports.size(),originToDestinationMap.size());
                    }
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
        LOGGER.info("Completed processing all {} tasks with {} flights and {} failed routes. Shutting down executor...",
                totalTasks,flightsProcessed,failedRoutes.size());
        executorService.shutdown();
        LOGGER.info("Post-executor shutdown: {}/{} airports processed, {} failed routes added, {} flight number errors, {} processing errors",
                processedAirports.size(),originToDestinationMap.size(),failedRoutes.size(),flightNumberErrors.size(),processingErrors);
        dropAndUpsertAirlineAirports(airlineToAirportCodes);
        printResultsPostFutures(processedAirports.size(),originToDestinationMap.size(),failedRoutes, totalFlightCreates,
                totalFlightPatches,totalFlightSkips,flightNumberErrors);
    }

    private static void dropAndUpsertAirlineAirports(Map<Airline, Set<String>> airlineToAirportCodes) {
        airlineToAirportCodes.forEach((airline,airportCodes)-> {
            // clear airline airports
            Either<ServiceError, Integer> deleteEither = airlineService.batchDeleteAirline(airline);
            if (deleteEither.isLeft()) {
                LOGGER.error("failed to batch DELETE airline {} from voyager services",airline.name());
            } else {
                LOGGER.info("successful batch DELETE of {} records for airline {}",deleteEither.get(),airline.name());
            }
            // batchupsert airport codes
            AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().airline(airline.name())
                    .iataList(new ArrayList<>(airportCodes)).isActive(true).build();
            Either<ServiceError, List<AirlineAirport>> eitherUpsert = airlineService.batchUpsert(airlineBatchUpsert);
            if (eitherUpsert.isLeft()) {
                LOGGER.error("failed to batch UPSERT {} records for airline {} to voyager services",
                        airportCodes.size(),airline.name());
            } else {
                LOGGER.info("successful batch UPSERT of {} records for airline {}",
                        eitherUpsert.get().size(),airline.name());
                eitherUpsert.get().forEach(airlineAirport -> LOGGER.trace(airlineAirport.toString()));
            }
        });
    }

    private static Either<TaskFailure,TaskResult> fetchAirportScheduleTask(String airportCode1, String airportCode2,
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

            int processedFlights = processAirportScheduleMapped(airportCode1,airportCode2,airportScheduleOption.get(),
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

    private static void printResultsPostFutures(int processedCount, int startingCount,
                                                List<TaskFailure> errorsOnRoutes,
                                                AtomicInteger totalFlightCreates,
                                                AtomicInteger totalFlightPatches, AtomicInteger totalFlightSkips,
                                                List<TaskResult> flightNumberErrors) {
        LOGGER.info("completed processing {}/{} airport codes with {} flights created, {} flights skipped, {} flights patched",
                processedCount, startingCount, totalFlightCreates.get(),totalFlightSkips.get(),
                totalFlightPatches.get());
        LOGGER.info("******************************************");

        if (errorsOnRoutes.isEmpty()) {
            String message = "no errors on airport routes";
            LOGGER.info(message);
            ConstantsDatasync.writeSetLineByLine(Set.of(message),ConstantsDatasync.FAILED_AIRPORT_SCHEDULE_FILE);
        } else {
            Set<String> errorRoutes = new HashSet<>();
            errorsOnRoutes.forEach(taskFailure -> {
                errorRoutes.add(String.format("%s:%s",taskFailure.getOrigin(),taskFailure.getDestination()));
                LOGGER.info("failed fetch route {}:{} with service error: {}",taskFailure.getOrigin(),
                        taskFailure.getDestination(),taskFailure.getServiceError().getException().getMessage());
            });
            ConstantsDatasync.writeSetLineByLine(errorRoutes,ConstantsDatasync.FAILED_AIRPORT_SCHEDULE_FILE);
        }
        LOGGER.info("******************************************");

        if (airportsReference.getMissingAirportMap().isEmpty()) {
            LOGGER.info("no airports missing from voyager services for these airlines");
        } else {
            LOGGER.info(String.format("%d airports missing from voyager services",
                    airportsReference.getMissingAirportMap().size()));
            airportsReference.getMissingAirportMap().forEach(LOGGER::info);
            Set<String> missingAirports = fetchAndBuildOutMissingAirports();
            ConstantsDatasync.writeSetLineByLine(missingAirports,MISSING_AIRPORTS_FILE);
        }
        LOGGER.info("******************************************");

        if (!airportsReference.getSkippedNonCivilMap().isEmpty()) {
            airportsReference.getSkippedNonCivilMap().forEach((iata, airportFR) -> {
                Airport existing = airportsReference.getAirportCodeMap().get(iata);
                LOGGER.info("{} airport routes skipped, voyager services marks as {} type",
                        iata,existing.getType().name());
            });
            ConstantsDatasync.writeSetLineByLine(airportsReference.getSkippedNonCivilMap().keySet(),NON_CIVIL_AIRPORTS_FILE);
        } else {
            LOGGER.info("no routes with non-CIVIL airports were fetched");
        }
        LOGGER.info("******************************************");

        if (!airportsReference.getMissingAirportFromRoute().isEmpty()) {
            LOGGER.error("{} airports from create routes skipped",airportsReference.getMissingAirportFromRoute().size());
            airportsReference.getMissingAirportFromRoute().forEach(LOGGER::error);
        } else {
            LOGGER.info("no airports were missing when create routes called");
        }
        LOGGER.info("******************************************");

        if (flightNumberErrors.isEmpty()) {
            LOGGER.info("no flight number errors");
        } else {
            flightNumberErrors.forEach(taskResult -> {
                LOGGER.info("{} flights from {}:{} failed during processing",
                        taskResult.getFlightNumberErrors().size(),taskResult.getOrigin(),taskResult.getDestination());
                taskResult.getFlightNumberErrors().forEach(LOGGER::info);
                LOGGER.info("-------------------------------------------------");
            });
        }
    }

    private static Set<String> fetchAndBuildOutMissingAirports() {
        Set<String> missingAirports = new HashSet<>();
        airportsReference.getMissingAirportMap().forEach((iata,airportFR )-> {
            String name = airportFR.getName();
            String city = airportFR.getCity();
            String subdivision = null;
            String countryCode = null;
            Double latitude = airportFR.getLat();
            Double longitude = airportFR.getLon();
            AirportType airportType = null;
            ZoneId zoneId = null;

            Either<ServiceError, Option<AirportDetailsFR>> airportDetailsEither = FlightRadarService.fetchAirportDetails(iata);
            if (airportDetailsEither.isRight() && airportDetailsEither.get().isDefined()
                    && airportDetailsEither.get().get().getDetails() != null
                    && airportDetailsEither.get().get().getDetails().getPosition() != null) {
                DetailsFR detailsFR = airportDetailsEither.get().get().getDetails();
                if (StringUtils.isBlank(name)) name = detailsFR.getName();
                PositionPR positionPR = detailsFR.getPosition();
                if (latitude == null) latitude = positionPR.getLatitude();
                if (longitude == null) longitude = positionPR.getLongitude();
                if (StringUtils.isBlank(city) && positionPR.getRegion() != null) {
                    city = positionPR.getRegion().getCity();
                }
                if (detailsFR.getTimezone() != null && StringUtils.isNotBlank(detailsFR.getTimezone().getZoneId()))
                zoneId = ZoneId.of(detailsFR.getTimezone().getZoneId());
            }

            if (latitude == null || longitude == null) {
                LOGGER.info("no latitude/longitude data for {}, skipping",iata);
                return;
            }
            Either<ServiceError,List<GeoName>> nearbyEither = GeoNamesService.findNearbyPlaces(latitude,longitude);
            if (nearbyEither.isRight() && !nearbyEither.get().isEmpty()) {
                GeoName firstResult = nearbyEither.get().get(0);
                if (StringUtils.isBlank(city)) name = firstResult.getName();
                subdivision = firstResult.getAdminName1();
                countryCode = firstResult.getCountryCode();
            }

            if (zoneId == null) {
                Either<ServiceError, Timezone> timezoneEither = GeoNamesService.getTimezone(latitude, longitude);
                if (timezoneEither.isRight()) {
                    Timezone timezone = timezoneEither.get();
                    zoneId = ZoneId.of(timezone.getTimezoneId());
                }
            }

            Either<ServiceError, AirportCH> airportCHEither = ChAviationService.getAirportCH(iata);
            if (airportCHEither.isRight()) {
                airportType = airportCHEither.get().getType();
            }

            if (StringUtils.isBlank(name) || StringUtils.isBlank(countryCode) || airportType == null) {
                LOGGER.info("no name/countryCode/type data for {}, skipping",iata);
                return;
            }

            Airport airport = Airport.builder().iata(iata).name(name).city(city).subdivision(subdivision).countryCode(countryCode)
                    .latitude(latitude).longitude(longitude).type(airportType).zoneId(zoneId).build();
            LOGGER.info("{} airport built from services: {}",iata,airport.toString());
            missingAirports.add(airport.toString());
        });
        return missingAirports;
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

                Option<Airline> airlineOption = resolveAirline(url,flightNumber);
                if (airlineOption.isEmpty()) {
                    LOGGER.trace("ignoring unmapped airline {} during route {}:{}, url '{}'",
                            airlineFR.getName(),processingAirportCode,iata,airlineFR.getUrl());
                    return;
                }
                Airline airline = airlineOption.get();
                LOGGER.trace("processing {} flight",airline.name());
                String origin = isOrigin ? processingAirportCode : iata;
                String destination = isOrigin ? iata : processingAirportCode;
                Either<String,Integer> flightEither = buildAndSaveFlight(plannedFR,origin,destination,
                        flightNumber,airline,isOrigin);
                if (flightEither.isLeft()) {
                    LOGGER.error("adding flight number {} to error flight numbers",flightNumber);
                    flightErrorNumbers.add(flightNumber);
                    return;
                }

                int value = flightEither.get();
                if (value > 0) {
                    localCreates[0]++;
                } else {
                    Set<String> airlineAirportCodes = airlineToAirportCodes.getOrDefault(airline,
                            ConcurrentHashMap.newKeySet());
                    airlineAirportCodes.add(origin);
                    airlineAirportCodes.add(destination);
                    airlineToAirportCodes.put(airline,airlineAirportCodes);
                    if (value < 0) localPatches[0]++;
                    else localSkips[0]++;
                }
            });
        });
    }

    private static Option<Airline> resolveAirline(String url, String flightNumber) {
        try {
            return Option.of(Airline.fromPathVariableFR(url));
        } catch (IllegalArgumentException e) {
            if (!url.equals("jl-jtl")) return Option.none(); // jet linx shares jl iata
            return Option.of(Airline.JAPAN);
        }
    }

    private static Either<String, Integer> buildAndSaveFlight(PlannedFR plannedFR, String origin, String destination,
                                                              String flightNumber, Airline airline,boolean isOrigin) {
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
                createValue,isOrigin);
        if (flightEither.isLeft()) return Either.left(flightEither.getLeft());
        return Either.right(createValue.get());
    }

    private static Either<String, Flight> fetchOrCreateFlight(Integer routeId,
                                                              String flightNumber,
                                                              Airline airline,
                                                              FlightTimeFR flightTimeFR,
                                                              AtomicInteger createValue,
                                                              boolean isOrigin) {
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
                    .isActive(false)
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
            LOGGER.trace("successfully created flight: " + created.get().toString());
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
            createValue.set(-1);
            return Either.right(patched);
        }
        LOGGER.trace("skipping flight patch with no new data: {}",existing);
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

    private static Map<String, Set<String>> fetchAirportToAirportMapWithFutures() {
        long startTime = System.currentTimeMillis();
        ExecutorService fetchAirlineExecutor = Executors.newFixedThreadPool(flightSyncConfig.getThreadCount());
        CompletionService<Either<Airline,AirlineResult>> airlineCompletionService =
                new ExecutorCompletionService<>(fetchAirlineExecutor);

        List<Future<Either<Airline,AirlineResult>>> airlineFutures = new ArrayList<>();
        Map<String,Set<String>> originToDestinationsMap = new ConcurrentHashMap<>();

        for (Airline airline : Airline.values()) {
            airlineFutures.add(airlineCompletionService.submit(() -> {
                return FlightRadarService.extractAirlineRoutes(airline).mapLeft(serviceError -> {
                    LOGGER.error("failed to fetch airline {} routes, error: {}",airline.name(),
                            serviceError.getException().getMessage());
                    return airline;
                }).map(routeFRList -> {
                    routeFRList.forEach(routeFR ->
                            addFilteredCodesToMap(routeFR,airline,originToDestinationsMap));
                    return new AirlineResult(airline,routeFRList.size());
                });
            }));
        }

        int completed = 0;
        int getErrors = 0;
        int tasks = airlineFutures.size();
        while (completed < tasks) {
            try {
                Future<Either<Airline,AirlineResult>> future = airlineCompletionService.take();
                Either<Airline,AirlineResult> result = future.get();
                completed++;
                if (result.isLeft()) {
                    Airline failedAirline = result.getLeft();
                    LOGGER.error("Fetch {} airline failed on task {}/{}, added to airportReference",
                            failedAirline.name(),completed,tasks);
                    airportsReference.getFailedFetchAirlineList().add(failedAirline);
                } else {
                    AirlineResult airlineResult = result.get();
                    LOGGER.info("fetch {} airline task {}/{} successfully completed with {} routes",
                            airlineResult.airline.name(),completed,tasks,airlineResult.routeCount);
                }
            } catch (ExecutionException | InterruptedException e) {
                completed++;
                getErrors++;
                LOGGER.error("Failed to get future for an airline on task {}/{}, get error count: {}, error: {}",
                        completed,tasks,getErrors,e.getMessage());
            }
        }

        fetchAirlineExecutor.shutdown();
        long duration = System.currentTimeMillis() - startTime;
        int sec = (int) (duration/1000);
        int min = sec/60;
        sec %= 60;
        LOGGER.info("loaded {} airport codes to process after {}ms ({}min {}sec)\n**********************************",
                originToDestinationsMap.size(),duration,min,sec);
        return originToDestinationsMap;
    }

    private static void addFilteredCodesToMap(RouteFR routeFR, Airline airline, Map<String, Set<String>> originToDestinationsMap) {

        if (routeFR.getAirport1() == null || StringUtils.isBlank(routeFR.getAirport1().getIata())
                || routeFR.getAirport2() == null || StringUtils.isBlank(routeFR.getAirport2().getIata())) {
            return;
        }
        String airportCode1 = routeFR.getAirport1().getIata();
        String airportCode2 = routeFR.getAirport2().getIata();
        if (airportCode2.equals(airportCode1)) return;

        if (airportsReference.getMissingAirportMap().containsKey(airportCode1)) {
            LOGGER.info("missing airport {} appeared again in route {}:{}, skipping",airportCode1,airportCode1,airportCode2);
            return;
        }

        if (!airportsReference.getAirportCodeMap().containsKey(airportCode1)) {
            airportsReference.getMissingAirportMap().put(airportCode1,routeFR.getAirport1());
            LOGGER.info("adding missing airport {} from {}:{} in {} routes to missing airport map, not in voyager services",
                    airportCode1,airportCode1,airportCode2,airline.name());
            return;
        }
        if (!airportsReference.getCivilAirportCodeMap().containsKey(airportCode1)) {
            Airport existing = airportsReference.getAirportCodeMap().get(airportCode1);
            airportsReference.getSkippedNonCivilMap().put(airportCode1,routeFR.getAirport1());
            LOGGER.info("skipping non-CIVIL airport {} in {}:{} in {} routes, has type {} in voyager services, added to non-CIVIL map",
                    airportCode1,airportCode1,airportCode2,airline,existing.getType().name());
            return;
        }


        if (airportsReference.getMissingAirportMap().containsKey(airportCode2)) {
            LOGGER.info("missing airport {} appeared again in route {}:{}, skipping",airportCode2,airportCode1,airportCode2);
            return;
        }
        if (!airportsReference.getAirportCodeMap().containsKey(airportCode2)) {
            airportsReference.getMissingAirportMap().put(airportCode2,routeFR.getAirport2());
            LOGGER.info("adding missing airport {} from {}:{} in {} routes to missing airport map, not in voyager services",
                    airportCode2,airportCode1,airportCode2,airline.name());
            return;
        }
        if (!airportsReference.getCivilAirportCodeMap().containsKey(airportCode2)) {
            Airport existing = airportsReference.getAirportCodeMap().get(airportCode2);
            airportsReference.getSkippedNonCivilMap().put(airportCode2,routeFR.getAirport2());
            LOGGER.info("skipping non-CIVIL airport {} in {}:{} in {} routes, has type {} in voyager services, added to non-CIVIL map",
                    airportCode2,airportCode1,airportCode2,airline,existing.getType().name());
            return;
        }
        Set<String> destinationSet = originToDestinationsMap.getOrDefault(airportCode1,new HashSet<>());
        destinationSet.add(airportCode2);
        originToDestinationsMap.put(airportCode1,destinationSet);
    }

    private static void buildoutAirportReference(AirportService airportService) {
        airportsReference.getAllAirportCodes().addAll(fetchAllIataCodes(airportService));
        airportsReference.getAirportCodeMap().putAll(fetchAndBuildAirportMap(airportService));
        LOGGER.info(String.format("airportReference loaded with %d airport codes to %d airports in map",
                airportsReference.getAllAirportCodes().size(),airportsReference.getAirportCodeMap().size()));

        airportsReference.getCivilAirportCodes().addAll(fetchCivilIataCodes(airportService));
        airportsReference.getCivilAirportCodeMap().putAll(fetchAndBuildCivilAirportMap(airportService));
        LOGGER.info(String.format("airportReference loaded with %d CIVIL airport codes to %d CIVIL airports in map",
                airportsReference.getCivilAirportCodes().size(),airportsReference.getCivilAirportCodeMap().size()));
    }

    private static List<String> fetchAllIataCodes(AirportService airportService) {
        return airportService.getIATACodes().get();
    }

    private static List<String> fetchCivilIataCodes(AirportService airportService) {
        return airportService.getIATACodes(IataQuery.builder()
                .withAirportTypeList(List.of(AirportType.CIVIL)).build()).get();
    }

    private static Map<String, Airport> fetchAndBuildAirportMap(AirportService airportService) {
        List<Airport> existingAirports = airportService.getAirports().get();
        Map<String,Airport> airportMap = new ConcurrentHashMap<>();
        existingAirports.forEach(airport -> airportMap.put(airport.getIata(),airport));
        return airportMap;
    }

    private static Map<String, Airport> fetchAndBuildCivilAirportMap(AirportService airportService) {
        List<Airport> existingAirports = airportService.getAirports(AirportQuery.builder()
                .withTypeList(List.of(AirportType.CIVIL)).build()).get();
        Map<String,Airport> airportMap = new ConcurrentHashMap<>();
        existingAirports.forEach(airport -> airportMap.put(airport.getIata(),airport));
        return airportMap;
    }
}
