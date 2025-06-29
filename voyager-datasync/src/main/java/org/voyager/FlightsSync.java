package org.voyager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.http.RetryHttpClient;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportType;
import org.voyager.model.datasync.search.AirportScheduleFR;
import org.voyager.model.datasync.search.CountryFR;
import org.voyager.model.datasync.search.FlightTimeFR;
import org.voyager.model.datasync.search.PlannedFR;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.*;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.voyager.utils.ConstantsLocal.*;

public class FlightsSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightsSync.class);
    private static RouteService routeService;
    private static AirportService airportService;
    private static FlightService flightService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        Airline airline = datasyncProgramArguments.getAirline();

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,
                maxConcurrentRequests,voyagerAuthorizationToken);
        Voyager voyager = new Voyager(voyagerConfig);
        routeService = voyager.getRouteService();
        flightService = voyager.getFlightService();
        airportService = voyager.getAirportService();

        Either<ServiceError, List<Airport>> airportsEither = airportService.getAirports(AirportType.CIVIL);
        if (airportsEither.isLeft()) {
            Exception exception = airportsEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        boolean fromVoyagerAPI = false;
        processSychronously(airline,airportsEither.get(),fromVoyagerAPI);
    }

    private static void processSychronously(Airline airline, List<Airport> airports, boolean fromVoyagerAPI) {
        Set<String> airlineAirports = ConstantsLocal.loadCodesFromListFile(AIRLINE_PROCESSED_FILE);
        Set<String> nonAirlineAirports = ConstantsLocal.loadCodesFromListFile(NON_AIRLINE_PROCESSED_FILE);
        Set<String> toProcess = null;
        if (fromVoyagerAPI) {
            toProcess = airports.stream().map(Airport::getIata)
                    .filter(iata -> !airlineAirports.contains(iata) && !nonAirlineAirports.contains(iata))
                    .collect(Collectors.toSet());
        } else {
            toProcess = ConstantsLocal.loadCodesFromListFile(ROUTE_AIRPORTS_FILE);
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
        ObjectMapper om = new ObjectMapper();
        boolean requestRateLimited = false;
        int totalFlightCreates = 0;
        int totalFlightPatches = 0;
        int totalFlightSkips = 0;
        for (String iata : toProcess) {
            processed++;
            LOGGER.info(String.format("processing %d/%d airports",processed,toProcess.size()));
            Either<ServiceError,String> either = FlightRadarService.fetchAirportResponse(iata,airline);
            if (either.isLeft()) {
                Exception exception = either.getLeft().getException();
                LOGGER.error(String.format("failed to fetch %s airport, error: %s",
                        iata,exception.getMessage()));
                if (exception instanceof RetryHttpClient.RetryLimitExceededException) {
                    requestRateLimited = true;
                    break;
                }
                fetchAirportErrors.add(iata);
            } else {
                String jsonBody = either.get();
                if (StringUtils.isBlank(jsonBody) || jsonBody.equals("[]")) {
                    LOGGER.info(String.format("%s airport returns no flights for airline %s",
                            iata,airline.name()));
                    nonAirlineAirports.add(iata);
                } else {
                    try {
                        AirportScheduleFR airportScheduleFR = om.readValue(jsonBody, AirportScheduleFR.class);
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
                    } catch (JsonProcessingException e) {
                        LOGGER.error(String.format("failed to map %s airport with error: %s, schedule: %s",
                                iata, e.getMessage(), jsonBody), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (requestRateLimited) {
            LOGGER.info("request rate limited, writing processed airports to target files");
            ConstantsLocal.writeSetLineByLine(airlineAirports, AIRLINE_PROCESSED_FILE);
            ConstantsLocal.writeSetLineByLine(nonAirlineAirports, NON_AIRLINE_PROCESSED_FILE);
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
        Either<ServiceError, Route> routeEither = fetchOrCreateRoute(origin,destination,routeService,flightNumber);
        if (routeEither.isLeft()) {
            Exception exception = routeEither.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
            return Either.left(flightNumber);
        }
        Route route = routeEither.get();
        Integer routeId = route.getId();

        String latestDate = plannedFR.getDateToTimeMap().keySet()
                .stream().max(Comparator.naturalOrder()).orElse(null);
        if (StringUtils.isBlank(latestDate)) {
            LOGGER.error(String.format("no latest date for flight %s with origin %s, plannedFR: %s",
                    flightNumber,origin,plannedFR));
            return Either.left(flightNumber);
        }

        String time = plannedFR.getDateToTimeMap().get(latestDate).getTime();
        String departureDateString = null;
        String departureTimeString = null;
        String arrivalDateString = null;
        String arrivalTimeString = null;
        if (isOrigin) {
            departureDateString = latestDate;
            departureTimeString = time;
        } else {
            arrivalDateString = latestDate;
            arrivalTimeString = time;
        }

        AtomicInteger createValue = new AtomicInteger(0);
        Either<String,Flight> flightEither = fetchOrCreateFlight(flightService,routeId,flightNumber,airline,
                plannedFR.getDateToTimeMap().get(latestDate),createValue,isOrigin);
        if (flightEither.isLeft()) return Either.left(flightEither.getLeft());

        Flight flight = flightEither.get();
        if (createValue.get() > 0)
            LOGGER.info(String.format("successfully created flight %s",flight));
        else if (createValue.get() < 0)
            LOGGER.info(String.format("successfully patched flight %s",flight));
        else
            LOGGER.info(String.format("successfully skipped existing flight %s",flight));

        if (route.getFlightIds().contains(flight.getId())) {
            LOGGER.info(String.format("skipping route %d patch, already contains flight id %d",
                    route.getId(),flight.getId()));
        } else {
            List<Integer> flightIds = route.getFlightIds();
            flightIds.add(flight.getId());
            RoutePatch routePatch = RoutePatch.builder().flightIds(flightIds).build();
            Either<ServiceError,Route> patchEither = routeService.patchRoute(route,routePatch);
            if (patchEither.isLeft()) return Either.left(flightNumber);
            LOGGER.info(String.format("successfully patched route %d, added flight id %d",
                    route.getId(),flight.getId()));
        }
        return Either.right(createValue.get());
    }

    private static Either<String, Flight> fetchOrCreateFlight(FlightService flightService,
                                                              Integer routeId,
                                                              String flightNumber,
                                                              Airline airline,
                                                              FlightTimeFR flightTimeFR,
                                                              AtomicInteger createValue, boolean isOrigin) {
        Either<ServiceError,List<Flight>> flightsEither = flightService.getFlights(routeId,flightNumber);
        if (flightsEither.isLeft()) {
            Exception exception = flightsEither.getLeft().getException();
            LOGGER.error(exception.getMessage(),exception);
            return Either.left(flightNumber);
        }
        List<Flight> flights = flightsEither.get();
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
        if (flights.isEmpty()) { // createFlight
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
        if (flights.size() > 1) {
            LOGGER.error(String.format("route id '%d' and flight number '%s' returns multiple flights",
                    routeId, flightNumber));
            return Either.left(flightNumber);
        }
        // check if patch needed
        Flight existing = flights.get(0);
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
            Either<ServiceError, Flight> patchEither = flightService.patchFlight(existing, flightPatch);
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
                                                    RouteService routeService,
                                                    String flightNumber) {
        Either<ServiceError, List<Route>> routesEither = routeService.getRoutes(origin,destination);
        if (routesEither.isLeft())
            return Either.left(routesEither.getLeft());
        List<Route> routeList = routesEither.get();
        if (routeList.isEmpty()) { // create new route
            RouteForm routeForm = RouteForm.builder()
                    .origin(origin)
                    .destination(destination)
                    .build();
            return routeService.createRoute(routeForm);
        }
        if (routeList.size() > 1)
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                new ServiceException(String.format("%s origin and %s destination returned multiple routes",
                        origin,destination))));
        return Either.right(routeList.get(0));
    }
}
