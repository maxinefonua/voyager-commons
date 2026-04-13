package org.voyager.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sdk.service.*;
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
import java.util.Map;

public class AirlineSync {
    private static AirlineSyncConfig airlineSyncConfig;
    private static AirlineProcessor airlineProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSync.class);
    // syncs airlines and airline routes
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        init(args);
        List<Airline> airlinesToProcess = new ArrayList<>(airlineSyncConfig.getAirlineList());
        airlinesToProcess.sort(Comparator.comparing(Airline::name));
        airlinesToProcess.forEach(airlineProcessor::process);
        long durationMs = System.currentTimeMillis()-startTime;
        int sec = (int) (durationMs/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    private static void init(String[] args) {
        airlineSyncConfig = new AirlineSyncConfig(args);
        LOGGER.info("initializing {} with args: {}",
                AirlineSync.class.getSimpleName(),String.join(" ", airlineSyncConfig.toArgs()));
        VoyagerServiceRegistry.initialize(airlineSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        AirlineService airlineService = voyagerServiceRegistry.get(AirlineService.class);
        AirportService airportService = voyagerServiceRegistry.get(AirportService.class);
        RouteService routeService = voyagerServiceRegistry.get(RouteService.class);
        RouteSyncService routeSyncService = voyagerServiceRegistry.get(RouteSyncService.class);
        GeoService geoService = voyagerServiceRegistry.get(GeoService.class);
        AirportReference airportReference = new AirportReferenceImpl(airportService);
        RouteProcessor routeProcessor = new RouteProcessorImpl(routeService,routeSyncService,airportReference);
        airlineProcessor = new AirlineProcessorImpl(airportReference,routeService,geoService,routeProcessor);
    }
}
