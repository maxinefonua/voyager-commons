package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.AirportsSyncConfig;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.airport.AirportCH;
import org.voyager.service.ChAviationService;
import org.voyager.service.AirportService;
import org.voyager.service.impl.VoyagerServiceRegistry;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportsSync {
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        AirportsSyncConfig airportsSyncConfig = initAirportSync(args);
        List<String> iataList = fetchIataCodesToProcess(airportsSyncConfig);
        processAirpots(iataList);
    }

    private static List<String> fetchIataCodesToProcess(AirportsSyncConfig airportsSyncConfig) {
        List<AirportType> processTypeList = airportsSyncConfig.getAirportTypeList();
        long start = System.currentTimeMillis();
        Either<ServiceError,List<String>> either = airportService.getIATACodes(processTypeList);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        LOGGER.info(String.format("%s ms elapsed while fetching airports from Voyager",
                System.currentTimeMillis()-start));
        List<String> iataCodes = either.get();
        if (processTypeList.contains(AirportType.CIVIL) && iataCodes.size() < 5000) {
            throw new RuntimeException("List of airports from Voyager has a size of %d. " +
                    "Requires investigation or reload of airports prior to processing");
        }
        return iataCodes;
    }

    private static AirportsSyncConfig initAirportSync(String[] args) {
        AirportsSyncConfig airportsSyncConfig = new AirportsSyncConfig(args);
        Integer maxConcurrentRequests = airportsSyncConfig.getThreadCount();
        VoyagerConfig voyagerConfig = airportsSyncConfig.getVoyagerConfig();
        VoyagerServiceRegistry.initialize(voyagerConfig);
        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        airportService = VoyagerServiceRegistry.getInstance().get(AirportService.class);
        return airportsSyncConfig;
    }

    private static void processAirpots(List<String> iataList) {
        AtomicInteger patchesMade = new AtomicInteger(0);
        AtomicInteger skippedMatches = new AtomicInteger(0);
        AtomicInteger skippedFromChError = new AtomicInteger(0);
        long prepatch = System.currentTimeMillis();
        patchAirportsUsingChAviation(iataList,patchesMade,skippedMatches,skippedFromChError);
        long seconds = (System.currentTimeMillis()-prepatch)/1000;
        long minutes = seconds/60;
        seconds %= 60;
        LOGGER.info(String.format("process duration: %d minutes %d seconds for %d patches made, " +
                        "%d airports skipped due to matching type, and %d airports skipped due to errors, " +
                        "out of all %d codes processed",
                minutes,seconds, patchesMade.get(),
                skippedMatches.get(),skippedFromChError.get(),
                iataList.size()));
    }

    private static void patchAirportsUsingChAviation(List<String> iataList,
                                                     AtomicInteger patchesMade,
                                                    AtomicInteger skippedMatches,
                                                     AtomicInteger skippedFromChError) {
        List<Future<Either<String, AirportCH>>> airportFutures = getAirportFutures(iataList,skippedMatches,skippedFromChError);
        airportFutures.forEach(future -> {
            try {
                Either<String,AirportCH> result = future.get();
                if (result.isRight()) {
                    processAirportCh(result.get(),skippedMatches,patchesMade);
                } else {
                    patchAsUnverified(result.getLeft(),skippedMatches,skippedFromChError);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to get future: " + future);
            }
        });
        executorService.shutdown();
    }

    private static void patchAsUnverified(String iata, AtomicInteger skippedMatches, AtomicInteger skippedFromChError) {
        Airport airport = airportService.getAirport(iata).get();
        if (airport.getType().equals(AirportType.UNVERIFIED)) {
            LOGGER.debug("Skipping patch of already matching UNVERIFIED type: "+airport);
            skippedMatches.getAndIncrement();
        } else {
            AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
            Either<ServiceError, Airport> result = airportService.patchAirport(iata, airportPatch);
            if (result.isLeft()) {
                LOGGER.debug(String.format("ChAviationService lookup of '%s' failed. " +
                                "Subsequent patch to UNVERIFIED failed with error: %s",
                        iata, result.getLeft().getException().getMessage())
                );
                skippedFromChError.getAndIncrement();
            } else {
                LOGGER.info(String.format("Successfully patched UNVERIFIED type to airport '%s'",
                        result.get().toString()));
            }
        }
    }

    private static void processAirportCh(AirportCH airportCH, AtomicInteger skippedMatches, AtomicInteger patchesMade) {
        Airport airport = airportService.getAirport(airportCH.getIata()).get();
        if (airport.getType().equals(airportCH.getType())) {
            LOGGER.debug("Skipping patch of already matching type: "+airport);
            skippedMatches.getAndIncrement();
        } else {
            AirportPatch airportPatch = AirportPatch.builder().type(airportCH.getType().name()).build();
            Either<ServiceError, Airport> either = airportService.patchAirport(airport.getIata(), airportPatch);
            if (either.isLeft()) {
                LOGGER.error(String.format("Failed patch from ChAviationService: %s with error: %s",
                        airportPatch, either.getLeft().getException().getMessage()));
            } else {
                LOGGER.info("Successful patch from ChAviationService: " + either.get());
                patchesMade.getAndIncrement();
            }
        }
    }

    private static List<Future<Either<String,AirportCH>>> getAirportFutures(List<String> iataList,
                                                                            AtomicInteger skippedMatches,
                                                                            AtomicInteger skippedFromChError) {
        LOGGER.info("attempting to fetch " + iataList.size() + " IATA codes from ChAviationService");
        List<Future<Either<String,AirportCH>>> futureList = Collections.synchronizedList(new ArrayList<>());
        iataList.forEach(iata -> {
            Callable<Either<String,AirportCH>> taskWithResult = () -> {
                return ChAviationService.getAirportCH(iata).mapLeft(serviceError -> {
                    LOGGER.trace(String.format("fetch '%s' from ChAviationService failed with error: %s",
                            iata,serviceError.getException().getMessage()));
                    return iata;
                });
            };
            Future<Either<String,AirportCH>> future = executorService.submit(taskWithResult);
            futureList.add(future);
        });
        return futureList;
    }

    private static List<Airport> fetchVoyagerAirports() {
        Either<ServiceError, List<Airport>> result = airportService.getAirports();
        if (result.isLeft()) throw new RuntimeException(result.getLeft().getMessage(),result.getLeft().getException());
        return result.get();
    }
}
