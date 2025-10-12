package org.voyager.airline;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.FlightSyncConfig;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.AirportQuery;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportType;
import org.voyager.model.flightRadar.search.AirportScheduleFR;
import org.voyager.model.flightRadar.search.CountryFR;
import org.voyager.model.flightRadar.search.FlightTimeFR;
import org.voyager.model.flightRadar.search.PlannedFR;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.service.AirportService;
import org.voyager.service.RouteService;
import org.voyager.service.FlightService;
import org.voyager.service.FlightRadarService;
import org.voyager.service.impl.VoyagerServiceRegistry;
import org.voyager.utils.ConstantsDatasync;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static org.voyager.utils.ConstantsDatasync.*;

public class FlightsSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightsSync.class);
    private static RouteService routeService;
    private static AirportService airportService;
    private static FlightService flightService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        FlightSyncConfig flightSyncConfig = new FlightSyncConfig(args);
        Airline airline = flightSyncConfig.getAirline();
        VoyagerConfig voyagerConfig = flightSyncConfig.getVoyagerConfig();
        VoyagerServiceRegistry.initialize(voyagerConfig);
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        routeService = voyagerServiceRegistry.get(RouteService.class);
        flightService = voyagerServiceRegistry.get(FlightService.class);
        airportService = voyagerServiceRegistry.get(AirportService.class);

        AirportQuery airportQuery = AirportQuery.builder().withTypeList(List.of(AirportType.CIVIL)).build();
        Either<ServiceError, List<Airport>> airportsEither = airportService.getAirports(airportQuery);
        if (airportsEither.isLeft()) {
            Exception exception = airportsEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        boolean fromVoyagerAPI = false;
        processSychronously(airline,airportsEither.get(),fromVoyagerAPI);
    }

    private static void processSychronously(Airline airline, List<Airport> airports, boolean fromVoyagerAPI) {
        Set<String> airlineAirports = ConstantsDatasync.loadCodesFromListFile(AIRLINE_PROCESSED_FILE);
        Set<String> nonAirlineAirports = ConstantsDatasync.loadCodesFromListFile(NON_AIRLINE_PROCESSED_FILE);
        Set<String> toProcess;
        if (fromVoyagerAPI) {
            toProcess = airports.stream().map(Airport::getIata)
                    .filter(iata -> !airlineAirports.contains(iata) && !nonAirlineAirports.contains(iata))
                    .collect(Collectors.toSet());
        } else {
            toProcess = ConstantsDatasync.loadCodesFromListFile(ROUTE_AIRPORTS_FILE);
        }
        LOGGER.info(String.format("%d airports to process, before removed pre-processed airports",
                toProcess.size()));
        nonAirlineAirports.forEach(toProcess::remove);
        airlineAirports.forEach(toProcess::remove);
        LOGGER.info(String.format("%d airports to process, after removing pre-processed airports",
                toProcess.size()));
        Set<String> fetchAirportErrors = new HashSet<>();
        Set<String> flightNumberErrors = new HashSet<>();
        int processed = 0;
        boolean requestRateLimited = false;
        int totalFlightCreates = 0;
        int totalFlightPatches = 0;
        int totalFlightSkips = 0;
        for (String iata : toProcess) {
            processed++;
            LOGGER.info(String.format("processing %d/%d airports",processed,toProcess.size()));
            Either<ServiceError, Option<AirportScheduleFR>> either = FlightRadarService.extractAirportResponse(iata,airline);
            if (either.isLeft()) {
                if (either.getLeft().getHttpStatus().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                    requestRateLimited = true;
                    break;
                }
                Exception exception = either.getLeft().getException();
                LOGGER.error(String.format("failed to fetch %s airport, error: %s",
                        iata,exception.getMessage()));
                fetchAirportErrors.add(iata);
            } else {
                Option<AirportScheduleFR> airportScheduleFROption = either.get();
                if (airportScheduleFROption.isEmpty()) {
                    LOGGER.info(String.format("%s airport returns no flights for airline %s",
                            iata,airline.name()));
                    nonAirlineAirports.add(iata);
                } else {
                    AirportScheduleFR airportScheduleFR = airportScheduleFROption.get();
                    AtomicInteger patchedFlights = new AtomicInteger();
                    AtomicInteger createdFlights = new AtomicInteger();
                    AtomicInteger skippedFlights = new AtomicInteger();
                    LOGGER.info(String.format("successful conversion of airportScheduleFR for %s airport: %s",
                            iata, airportScheduleFR));
                    if (airportScheduleFR.getDepartures() != null) {
                        airportScheduleFR.getDepartures().forEach((countryName,countryFR) -> {
                            processCountryFlights(patchedFlights,createdFlights,skippedFlights,flightNumberErrors,
                                    countryName,countryFR,iata,airline,true,routeService,flightService);
                        });
                    }
                    if (airportScheduleFR.getArrivals() != null) {
                        airportScheduleFR.getArrivals().forEach((countryName,countryFR) -> {
                            processCountryFlights(patchedFlights,createdFlights,skippedFlights,flightNumberErrors,
                                    countryName,countryFR,iata,airline,false,routeService,flightService);
                        });
                    }
                    totalFlightCreates += createdFlights.get();
                    totalFlightPatches += patchedFlights.get();
                    totalFlightSkips += skippedFlights.get();
                    airlineAirports.add(iata);
                }
            }
        }
        if (requestRateLimited) {
            LOGGER.info("request rate limited, writing processed airports to target files");
            ConstantsDatasync.writeSetLineByLine(airlineAirports, AIRLINE_PROCESSED_FILE);
            ConstantsDatasync.writeSetLineByLine(nonAirlineAirports, NON_AIRLINE_PROCESSED_FILE);
        } else {
            LOGGER.info(String.format("successfully proccessed all airports with %d created flights," +
                    " %d patched flights, %d skipped flights",totalFlightCreates,totalFlightPatches,totalFlightSkips));
        }
        fetchAirportErrors.forEach(iata -> LOGGER.info(String.format("Error returned on fetch %s airport",iata)));
        flightNumberErrors.forEach(flightNumber -> LOGGER.info(String.format("Error returned on publish flight number %s",flightNumber)));
    }

    public static int processCountryFlights(AtomicInteger patchedFlights,
                                     AtomicInteger createdFlights,
                                     AtomicInteger skippedFlights,
                                     Set<String> flightNumberErrors,
                                     String countryName,
                                     CountryFR countryFR,
                                     String iata,
                                     Airline airline, boolean isOrigin,
                                     RouteService routeService,
                                     FlightService flightService) {
        AtomicInteger airportCount = new AtomicInteger();
        AtomicInteger flightCount = new AtomicInteger();
        countryFR.getIataToFlightsMap().forEach((airportCode, airportFlightFR) -> {
            airportFlightFR.getFlightNumberToPlannedMap().forEach((flightNumber, plannedFR) -> {
                if (airline.equals(Airline.DELTA) && flightNumber.startsWith("DLX")) return;
                Either<String,Integer> flightEither = buildFlight(plannedFR,
                        isOrigin,airline,iata,airportCode,
                        flightNumber,routeService, flightService);
                if (flightEither.isLeft()) {
                    flightNumberErrors.add(flightEither.getLeft());
                } else {
                    int value = flightEither.get();
                    if (value < 0) {
                        patchedFlights.getAndIncrement();
                    }
                    else if (value > 0) createdFlights.getAndIncrement();
                    else skippedFlights.getAndIncrement();
                    flightCount.getAndIncrement();
                }
            });
            if (isOrigin) {
                LOGGER.info(String.format("processed %s->%s for destination country %s with %d flights",
                        iata,airportCode,countryName,flightCount.get()));
            } else {
                LOGGER.info(String.format("processed %s->%s for origin country %s with %d flights",
                        airportCode,iata,countryName,flightCount.get()));
            }
            airportCount.getAndIncrement();
        });
        if (isOrigin) {
            LOGGER.info(String.format("processed %s flights with origin: %s to %d airports",
                    countryName, iata, airportCount.get()));
        } else {
            LOGGER.info(String.format("processed %s with destination: %s from %d airports",
                    countryName, iata, airportCount.get()));
        }
        return flightCount.get();
    }

    private static Either<String, Integer> buildFlight(PlannedFR plannedFR, boolean isOrigin, Airline airline,
                                                String iata, String airportCode, String flightNumber,
                                                RouteService routeService, FlightService flightService) {
        String origin = iata;
        String destination = airportCode;
        if (!isOrigin) {
            origin = airportCode;
            destination = iata;
        }
        Either<ServiceError, Route> routeEither = fetchOrCreateRoute(origin,destination,routeService);
        if (routeEither.isLeft()) {
            Exception exception = routeEither.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
            return Either.left(flightNumber);
        }
        Integer routeId = routeEither.get().getId();

        String latestDate = plannedFR.getDateToTimeMap().keySet()
                .stream().max(Comparator.naturalOrder()).orElse(null);
        if (StringUtils.isBlank(latestDate)) {
            LOGGER.error(String.format("no latest date for flight %s with origin %s, plannedFR: %s",
                    flightNumber,origin,plannedFR));
            return Either.left(flightNumber);
        }

        AtomicInteger createValue = new AtomicInteger(0);
        Either<String,Flight> flightEither = fetchOrCreateFlight(flightService,routeId,flightNumber,airline,
                plannedFR.getDateToTimeMap().get(latestDate),createValue,isOrigin);
        if (flightEither.isLeft()) return Either.left(flightEither.getLeft());
        return Either.right(createValue.get());
    }

    private static Either<String, Flight> fetchOrCreateFlight(FlightService flightService,
                                                              Integer routeId,
                                                              String flightNumber,
                                                              Airline airline,
                                                              FlightTimeFR flightTimeFR,
                                                              AtomicInteger createValue, boolean isOrigin) {
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
        FlightPatch flightPatch = FlightPatch.builder()
                .departureTimestamp(departureTimestamp)
                .departureOffset(departureOffset)
                .arrivalTimestamp(arrivalTimestamp)
                .arrivalOffset(arrivalOffset)
                .isActive(true)
                .build();
        if (!existing.getIsActive().equals(flightPatch.getIsActive())
                || (existing.getZonedDateTimeDeparture() == null && departureTimestamp != null)
                || (existing.getZonedDateTimeArrival() == null && arrivalTimestamp != null)) {
            Either<ServiceError, Flight> patchEither = flightService.patchFlight(existing.getId(), flightPatch);
            if (patchEither.isLeft()) {
                Exception exception = patchEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                return Either.left(flightNumber);
            }
            Flight patched = patchEither.get();
            LOGGER.info("successfully patched flight: " + patched.toString());
            createValue.set(-1);
            return Either.right(patched);
        }
        LOGGER.info("skipping flight patch with no new data: " + existing);
        return Either.right(existing);
    }

    private static Either<ServiceError,Route> fetchOrCreateRoute(String origin,
                                                    String destination,
                                                    RouteService routeService) {
        Either<ServiceError, Route> either = routeService.getRoute(origin, destination);
        if (either.isRight()) return either;
        // create route
        Airport startAirport = airportService.getAirport(origin).get();
        Airport endAirport = airportService.getAirport(destination).get();
        Double distanceKm = Airport.calculateDistanceKm(startAirport.getLatitude(),startAirport.getLongitude(),endAirport.getLatitude(),endAirport.getLongitude());
        RouteForm routeForm = RouteForm.builder()
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .build();
        return routeService.createRoute(routeForm);
    }
}
