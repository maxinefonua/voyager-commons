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
import org.voyager.sdk.service.AirlineService;
import org.voyager.sdk.service.FlightService;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;
import org.voyager.sync.service.*;
import org.voyager.sync.service.external.FlightRadarService;
import java.util.*;
import java.util.concurrent.*;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public class FlightProcessorImpl implements FlightProcessor {
    private final int threadCount;
    private final RouteSyncService routeSyncService;
    private final RouteProcessor routeProcessor;
    private final AirportScheduleProcessor airportScheduleProcessor;
    private final ResultProcessor resultProcessor;

    private ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightProcessorImpl.class);

    public FlightProcessorImpl(
            RouteSyncService routeSyncService, FlightService flightService, AirlineService airlineService,
            AirportReference airportReference, RouteService routeService, FlightSyncConfig flightSyncConfig,
            RouteProcessor routeProcessor){
        this.threadCount = flightSyncConfig.getThreadCount();
        this.routeSyncService = routeSyncService;
        this.routeProcessor = routeProcessor;
        this.airportScheduleProcessor = new AirportScheduleProcessorImpl(
                flightService,airportReference,routeProcessor,flightSyncConfig.getAirlineList());
        this.resultProcessor = new ResultProcessorImpl(flightSyncConfig,flightService,airlineService);
    }

    @Override
    public void process(List<Route> routeList) {
        executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<Either<AirportScheduleFailure, AirportScheduleResult>>> futureList = new ArrayList<>();
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
                                    LOGGER.trace("{}:{} returned no flights, successfully patched as completed",
                                            airportCode1, airportCode2);
                                    return right(new AirportScheduleResult(
                                            airportCode1, airportCode2, 0, 0, 0));
                                }
                                return airportScheduleProcessor.process(
                                        airportScheduleFROption.get(),airportCode1,airportCode2);
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
                    RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                            .status(Status.COMPLETED)
                            .build();
                    Route processingRoute = routeProcessor.fetchSavedCivilRoute(result.airportCode1,result.airportCode2);
                    Either<ServiceError, RouteSync> patchEither = routeSyncService.patchRouteSync(
                            processingRoute.getId(), routeSyncPatch);
                    if (patchEither.isLeft()) {
                        AirportScheduleFailure failedRouteSyncPatch = new AirportScheduleFailure(
                                result.airportCode1,result.airportCode2,patchEither.getLeft());
                        failureList.add(failedRouteSyncPatch);
                        LOGGER.error("task {}/{} failed to patch completed sync for route {}:{}, error: {}",
                                completedTasks,totalTasks,processingRoute.getOrigin(),processingRoute.getDestination(),
                                patchEither.getLeft().getException().getMessage());
                    } else {
                        LOGGER.trace("successfully patched as completed for route {}:{}",
                                processingRoute.getOrigin(),processingRoute.getDestination());
                        flightCreates += result.flightsCreated;
                        flightPatches += result.flightsPatched;
                        flightSkips += result.flightsSkipped;
                        LOGGER.info("task {}/{} completed for route {}:{} with {} creates, {} patches, {} skips",
                                completedTasks,totalTasks, result.airportCode1,result.airportCode2,
                                result.flightsCreated,result.flightsPatched,result.flightsSkipped);
                    }
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

        resultProcessor.process(failureList);
        shutdown();
    }

    private void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
