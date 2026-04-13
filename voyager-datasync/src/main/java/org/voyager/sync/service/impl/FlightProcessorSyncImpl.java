package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.Status;
import org.voyager.sdk.service.FlightService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.AirportScheduleProcessor;
import org.voyager.sync.service.FlightProcessor;
import org.voyager.sync.service.RouteProcessor;
import org.voyager.sync.service.external.FlightRadarService;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public class FlightProcessorSyncImpl implements FlightProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightProcessorSyncImpl.class);
    private final RouteSyncService routeSyncService;
    private final RouteProcessor routeProcessor;
    private final AirportScheduleProcessor airportScheduleProcessor;

    public FlightProcessorSyncImpl(
            RouteSyncService routeSyncService, FlightService flightService, AirportReference airportReference,
            FlightSyncConfig flightSyncConfig, RouteProcessor routeProcessor){
        this.routeSyncService = routeSyncService;
        this.routeProcessor = routeProcessor;
        this.airportScheduleProcessor = new AirportScheduleProcessorImpl(
                flightService,airportReference,routeProcessor, Arrays.stream(Airline.values()).toList());
    }

    @Override
    public void process(List<Route> routeList) {
        int totalTasks = routeList.size();
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger flightCreates = new AtomicInteger(0);
        AtomicInteger flightPatches = new AtomicInteger(0);
        AtomicInteger flightSkips = new AtomicInteger(0);
        Queue<AirportScheduleFailure> failureQueue = new ConcurrentLinkedQueue<>();

        routeList.forEach(route -> {
            fetchAndProcessAirportSchedule(
                    route,totalTasks,completedTasks,flightCreates,flightPatches,flightSkips,failureQueue);
        });

        LOGGER.info("flight processor completed with {} failures",failureQueue.size());
        while (!failureQueue.isEmpty()) {
            AirportScheduleFailure airportScheduleFailure = failureQueue.poll();
            LOGGER.error("{}:{} failed with error: {}",airportScheduleFailure.airportCode1,
                    airportScheduleFailure.airportCode2,
                    airportScheduleFailure.serviceError.getException().getMessage());
        }
    }

    private void fetchAndProcessAirportSchedule(
            Route route, int totalTasks, AtomicInteger completedTasks, AtomicInteger flightCreates, AtomicInteger flightPatches,
            AtomicInteger flightSkips, Queue<AirportScheduleFailure> failureQueue) {
        String airportCode1 = route.getOrigin();
        String airportCode2 = route.getDestination();
        Either<AirportScheduleFailure, AirportScheduleResult> either = fetchAirportScheduleEither(
                airportCode1,airportCode2,route);
        int currentCount = completedTasks.incrementAndGet();
        if (either.isLeft()) {
            AirportScheduleFailure failure = either.getLeft();
            failureQueue.add(failure);
            LOGGER.error("task {}/{} failed for route {}:{} with error: {}", currentCount, totalTasks,
                    failure.airportCode1, failure.airportCode2,
                    failure.serviceError.getException().getMessage());
        } else {
            AirportScheduleResult result = either.get();
            RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                    .status(Status.COMPLETED)
                    .build();
            Route processingRoute = routeProcessor.fetchSavedCivilRoute(result.airportCode1, result.airportCode2);
            Either<ServiceError, RouteSync> patchEither = routeSyncService.patchRouteSync(
                    processingRoute.getId(), routeSyncPatch);
            if (patchEither.isLeft()) {
                AirportScheduleFailure failedRouteSyncPatch = new AirportScheduleFailure(
                        result.airportCode1, result.airportCode2, patchEither.getLeft());
                failureQueue.add(failedRouteSyncPatch);
                LOGGER.error("task {}/{} failed to patch completed sync for route {}:{}, error: {}",
                        currentCount, totalTasks, processingRoute.getOrigin(), processingRoute.getDestination(),
                        patchEither.getLeft().getException().getMessage());
            } else {
                LOGGER.trace("successfully patched as completed for route {}:{}",
                        processingRoute.getOrigin(), processingRoute.getDestination());
                flightCreates.getAndAdd(result.flightsCreated);
                flightPatches.getAndAdd(result.flightsPatched);
                flightSkips.getAndAdd(result.flightsSkipped);
                LOGGER.info("task {}/{} completed for route {}:{} with {} creates, {} patches, {} skips",
                        currentCount, totalTasks, result.airportCode1, result.airportCode2,
                        result.flightsCreated, result.flightsPatched, result.flightsSkipped);
            }
        }
    }

    private Either<AirportScheduleFailure, AirportScheduleResult> fetchAirportScheduleEither(
            String airportCode1, String airportCode2, Route route) {
        return FlightRadarService.extractAirportResponseWithRetry(airportCode1, airportCode2)
                .mapLeft(serviceError ->
                        new AirportScheduleFailure(airportCode1, airportCode2, serviceError))
                .flatMap(airportScheduleFROption -> {
                    if (airportScheduleFROption.isEmpty()) {
                        RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                                .status(Status.COMPLETED)
                                .build();
                        Either<ServiceError, RouteSync> patchEither = routeSyncService.patchRouteSync(
                                route.getId(), routeSyncPatch);
                        if (patchEither.isLeft()) {
                            Exception exception = patchEither.getLeft().getException();
                            LOGGER.error("failed to patch route sync for empty route {}:{}, error: {}",
                                    airportCode1, airportCode2, exception.getMessage());
                            return left(new AirportScheduleFailure(airportCode1, airportCode2,
                                    patchEither.getLeft()));
                        }
                        LOGGER.trace("{}:{} returned no flights, successfully patched as completed",
                                airportCode1, airportCode2);
                        return right(new AirportScheduleResult(
                                airportCode1, airportCode2, 0, 0, 0));
                    }
                    return airportScheduleProcessor.process(
                            airportScheduleFROption.get(), airportCode1, airportCode2);
                });
    }
}
