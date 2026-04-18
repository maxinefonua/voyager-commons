package org.voyager.sync;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.route.Route;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sdk.service.FlightService;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.commons.error.ServiceError;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.FlightProcessor;
import org.voyager.sync.service.RouteProcessor;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.service.impl.FlightProcessorSyncImpl;
import org.voyager.sync.service.impl.AirportReferenceImpl;
import org.voyager.sync.service.impl.RouteProcessorImpl;
import java.util.List;

public class FlightSync {
    private static FlightSyncConfig flightSyncConfig;
    private static RouteService routeService;
    private static RouteSyncService routeSyncService;
    private static RouteProcessor routeProcessor;
    private static FlightService flightService;
    private static AirportService airportService;
    private static AirportReference airportReference;
    private static FlightProcessor flightProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        init(args);
        removePreRetentionDays(flightSyncConfig.getRetentionDays(),flightService);
        List<Route> toProcess = routeProcessor.fetchRoutesToProcess();
        if (toProcess.isEmpty()) {
            LOGGER.info("confirmed no pending routes to process, exiting");
        } else {
            flightProcessor.process(toProcess);
        }
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    private static void removePreRetentionDays(int retentionDays, FlightService flightService) {
        FlightBatchDelete flightBatchDelete = FlightBatchDelete.builder()
                .daysPast(String.valueOf(retentionDays)).build();
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

    private static void init(String[] args) {
        flightSyncConfig = new FlightSyncConfig(args);
        LOGGER.info("initializing {} with args: {}",
                FlightSync.class.getSimpleName(),String.join(" ", flightSyncConfig.toArgs()));
        VoyagerServiceRegistry.initialize(flightSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        routeService = voyagerServiceRegistry.get(RouteService.class);
        flightService = voyagerServiceRegistry.get(FlightService.class);
        airportService = voyagerServiceRegistry.get(AirportService.class);
        routeSyncService = voyagerServiceRegistry.get(RouteSyncService.class);
        airportReference = new AirportReferenceImpl(airportService);
        routeProcessor = new RouteProcessorImpl(routeService,routeSyncService,airportReference);
        flightProcessor = new FlightProcessorSyncImpl(
                routeSyncService,flightService,airportReference, routeProcessor);
    }
}
