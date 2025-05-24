package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.airport.AirportCH;
import org.voyager.service.ChAviationService;
import org.voyager.service.airport.AirportService;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class AirportsSync {
    private static Voyager voyager;
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static int SKIP_ROWS = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();

        VoyagerConfig voyagerConfig = new VoyagerConfig(VoyagerConfig.Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);

        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        voyager = new Voyager(voyagerConfig);
        airportService = voyager.getAirportService();

        long start = System.currentTimeMillis();
        List<Airport> voyagerAirports = fetchVoyagerAirports();
        LOGGER.info(String.format("%s ms elapsed while fetching airports from Voyager",System.currentTimeMillis()-start));
        if (voyagerAirports.size() < 5000) throw new RuntimeException("List of airports from Voyager has a size of %d. Requires investigation or reload of airports prior to processing");

        List<String> iataList = voyagerAirports.stream()
                .filter(airport -> airport.getType().equals(AirportType.UNVERIFIED))
                .skip(SKIP_ROWS)
                .limit(processLimit).map(Airport::getIata).toList();

        long prepatch = System.currentTimeMillis();
        int successes = patchAirportsUsingChAviation(iataList);
        LOGGER.info(String.format("patch duration: %d seconds for %d successful patches out of %d codes processed",
                (System.currentTimeMillis()-prepatch)/1000,successes,iataList.size()));
    }

    private static int patchAirportsUsingChAviation(List<String> iataList) {
        List<Future<Either<String, AirportCH>>> airportFutures = getAirportFutures(iataList);
        AtomicReference<Integer> success = new AtomicReference<>(0);
        airportFutures.forEach(future -> {
            try {
                Either<String,AirportCH> result = future.get();
                if (result.isRight()) {
                    AirportCH airportCH = result.get();
                    Airport airport = airportService.getAirport(airportCH.getIata()).get();
                    AirportPatch airportPatch = AirportPatch.builder().type(airportCH.getType().name()).build();
                    Either<ServiceError, Airport> either = airportService.patchAirport(airport.getIata(),airportPatch);
                    if (either.isLeft()) LOGGER.error("Failed patch from ChAviationService: " + airportPatch.toString() + "\n" + either.getLeft().getException().getMessage());
                    else {
                        LOGGER.info("Successful patch from ChAviationService: " + either.get());
                        success.getAndSet(success.get()+1);
                    }
                    LOGGER.info("***********");
                } else {
                    Airport airport = airportService.getAirport(result.getLeft()).get();
                    AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
                    Either<ServiceError, Airport> either = airportService.patchAirport(airport.getIata(),airportPatch);
                    if (either.isLeft()) LOGGER.error(String.format("Failed %s patch: %s\nError: %s",
                            airport.getIata(),airportPatch.toString(),either.getLeft().getException().getMessage()));
                    else LOGGER.info("Successful patch as UNVERIFIED: " + either.get());
                    LOGGER.info("***********");
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to get future: " + future);
            }
        });
        executorService.shutdown();
        return success.get();
    }

    private static List<Future<Either<String,AirportCH>>> getAirportFutures(List<String> iataList) {
        LOGGER.info("attempting to fetch " + iataList.size() + " IATA codes from ChAviationService");
        List<Future<Either<String,AirportCH>>> futureList = Collections.synchronizedList(new ArrayList<>());
        iataList.forEach(iata -> {
            Callable<Either<String,AirportCH>> taskWithResult = () -> {
                return ChAviationService.getAirportCH(iata).mapLeft(serviceError -> {
                    LOGGER.error(String.format("ChAviationService lookup of '%s' failed with error: %s, attempting to patch as UNVERIFIED",
                            iata, serviceError.getException().getMessage())
                    );
                    AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
                    Either<ServiceError,Airport> result = airportService.patchAirport(iata,airportPatch);
                    if (result.isLeft()) {
                        LOGGER.error(String.format("Failed to patch '%s' as UNVERIFIED. Error: %s",
                                iata,result.getLeft().getException().getMessage()));
                    } else {
                        LOGGER.info(String.format("Successfully patched UNVERIFIED type to airport '%s'",
                                result.get().toString()));
                    }
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
