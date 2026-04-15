package org.voyager.sync;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sdk.service.AirlineService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.config.AirlineSyncConfig;
import org.voyager.sync.service.AirlineProcessor;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.RouteProcessor;
import org.voyager.sync.service.impl.AirlineProcessorImpl;
import org.voyager.sync.service.impl.AirportReferenceImpl;
import org.voyager.sync.service.impl.RouteProcessorImpl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AirlineSync {
    private static AirlineProcessor airlineProcessor;
    private static AirlineService airlineService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        init(args);
        Either<ServiceError,List<Airline>> either = airlineService.getAirlines();
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(String.format("failed to get airlines, %s",exception.getMessage()),exception);
        }
        List<Airline> airlinesToProcess = new ArrayList<>(either.get());
        airlinesToProcess.sort(Comparator.comparing(Airline::name));
        AtomicInteger processed = new AtomicInteger(0);
        int total = airlinesToProcess.size();
        airlinesToProcess.forEach(airline -> {
            airlineProcessor.process(airline);
            LOGGER.info("{}/{} airlines complete",processed.incrementAndGet(),total);
        });
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    private static void init(String[] args) {
        AirlineSyncConfig airlineSyncConfig = new AirlineSyncConfig(args);
        LOGGER.info("initializing {} with args: {}",
                AirlineSync.class.getSimpleName(),String.join(" ", airlineSyncConfig.toArgs()));
        VoyagerServiceRegistry.initialize(airlineSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airlineService = voyagerServiceRegistry.get(AirlineService.class);
        AirportService airportService = voyagerServiceRegistry.get(AirportService.class);
        RouteService routeService = voyagerServiceRegistry.get(RouteService.class);
        RouteSyncService routeSyncService = voyagerServiceRegistry.get(RouteSyncService.class);
        GeoService geoService = voyagerServiceRegistry.get(GeoService.class);
        AirportReference airportReference = new AirportReferenceImpl(airportService);
        RouteProcessor routeProcessor = new RouteProcessorImpl(routeService,routeSyncService,airportReference);
        airlineProcessor = new AirlineProcessorImpl(airportReference,routeService,geoService,routeProcessor);
    }
}