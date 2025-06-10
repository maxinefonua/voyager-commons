package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.airport.AirportCH;
import org.voyager.service.ChAviationService;
import org.voyager.service.AirportService;
import org.voyager.service.Voyager;
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

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);

        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        voyager = new Voyager(voyagerConfig);
        airportService = voyager.getAirportService();

        Set<AirportType> filterTypes = extractFilterTypes(args);

        long start = System.currentTimeMillis();
        List<Airport> voyagerAirports = fetchVoyagerAirports();
        LOGGER.info(String.format("%s ms elapsed while fetching airports from Voyager",System.currentTimeMillis()-start));
        if (voyagerAirports.size() < 5000) throw new RuntimeException("List of airports from Voyager has a size of %d. Requires investigation or reload of airports prior to processing");

        List<String> iataList = voyagerAirports.stream()
                .filter(airport -> filterTypes.contains(airport.getType()))
                .skip(SKIP_ROWS)
                .limit(processLimit).map(Airport::getIata).toList();

        long prepatch = System.currentTimeMillis();
        AtomicReference<Integer> patchesMade = new AtomicReference<>(0);
        AtomicReference<Integer> skippedMatches = new AtomicReference<>(0);
        AtomicReference<Integer> skippedFromChError = new AtomicReference<>(0);
        patchAirportsUsingChAviation(iataList,patchesMade,skippedMatches,skippedFromChError);
        long seconds = (System.currentTimeMillis()-prepatch)/1000;
        long minutes = seconds/60;
        seconds %= 60;
        LOGGER.info(String.format("job duration: %d minutes %d seconds for %d patches made, " +
                        "%d matching airports skipped, and " +
                        "%d airports skipped due to errors out of %d codes processed",
                minutes,seconds,
                patchesMade.get(),skippedMatches.get(),skippedFromChError.get(),
                iataList.size()));
    }

    private static Set<AirportType> extractFilterTypes(String[] args) {
        Set<String> accepted = new HashSet<>();
        Arrays.stream(AirportType.values()).forEach(type -> accepted.add(type.name()));
        Set<AirportType> filterToProcess = new HashSet<>();
        for (String arg : args) {
            if (arg.contains("-")) continue;
            if (accepted.contains(arg.toUpperCase())) filterToProcess.add(AirportType.valueOf(arg.toUpperCase()));
        }
        if (filterToProcess.isEmpty()) {
            LOGGER.info(String.format("Filtering airports to process by default type: %s",AirportType.UNVERIFIED));
            filterToProcess.add(AirportType.UNVERIFIED);
        }
        return filterToProcess;
    }

    private static int patchAirportsUsingChAviation(List<String> iataList, AtomicReference<Integer> patchesMade, AtomicReference<Integer> skippedMatches, AtomicReference<Integer> skippedFromChError) {
        List<Future<Either<String, AirportCH>>> airportFutures = getAirportFutures(iataList,skippedFromChError);
        airportFutures.forEach(future -> {
            try {
                Either<String,AirportCH> result = future.get();
                if (result.isRight()) {
                    AirportCH airportCH = result.get();
                    Airport airport = airportService.getAirport(airportCH.getIata()).get();
                    if (airport.getType().equals(airportCH.getType())) {
                        LOGGER.debug("Skipping patch of already matching type: "+airport);
                        skippedMatches.getAndSet(skippedMatches.get()+1);
                    } else {
                        AirportPatch airportPatch = AirportPatch.builder().type(airportCH.getType().name()).build();
                        Either<ServiceError, Airport> either = airportService.patchAirport(airport.getIata(), airportPatch);
                        if (either.isLeft())
                            LOGGER.error(String.format("Failed patch from ChAviationService: %s with error: %s",
                                    airportPatch,either.getLeft().getException().getMessage()));
                        else {
                            LOGGER.info("Successful patch from ChAviationService: " + either.get());
                            patchesMade.getAndSet(patchesMade.get() + 1);
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to get future: " + future);
            }
        });
        executorService.shutdown();
        return patchesMade.get();
    }

    private static List<Future<Either<String,AirportCH>>> getAirportFutures(List<String> iataList, AtomicReference<Integer> skippedFromChError) {
        LOGGER.info("attempting to fetch " + iataList.size() + " IATA codes from ChAviationService");
        List<Future<Either<String,AirportCH>>> futureList = Collections.synchronizedList(new ArrayList<>());
        iataList.forEach(iata -> {
            Callable<Either<String,AirportCH>> taskWithResult = () -> {
                return ChAviationService.getAirportCH(iata).mapLeft(serviceError -> {
                    Airport airport = airportService.getAirport(iata).get();
                    if (airport.getType().equals(AirportType.UNVERIFIED)) {
                        LOGGER.debug("Skipping patch of already matching UNVERIFIED type: "+airport);
                        skippedFromChError.getAndSet(skippedFromChError.get()+1);
                    } else {
                        AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
                        Either<ServiceError, Airport> result = airportService.patchAirport(iata, airportPatch);
                        if (result.isLeft()) {
                            LOGGER.error(String.format("ChAviationService lookup of '%s' " +
                                            "failed with error: %s, " +
                                            "and failed to patch as UNVERIFIED with error: %s",
                                    iata,
                                    serviceError.getException().getMessage(),
                                    result.getLeft().getException().getMessage())
                            );
                        } else {
                            LOGGER.info(String.format("Successfully patched UNVERIFIED type to airport '%s'",
                                    result.get().toString()));
                        }
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
