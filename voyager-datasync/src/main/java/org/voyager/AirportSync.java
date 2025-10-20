package org.voyager;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.AirportSyncConfig;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.IataQuery;
import org.voyager.model.airport.*;
import org.voyager.model.flightRadar.airport.AirportDetailsFR;
import org.voyager.model.geoname.GeoName;
import org.voyager.model.geoname.Timezone;
import org.voyager.service.*;
import org.voyager.service.impl.VoyagerServiceRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportSync {
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static AirportSyncConfig airportSyncConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        init(args);
        List<String> iataList = fetchIataCodesToProcess();
        processAirports(iataList);
    }

    public static void init(String[] args) {
        airportSyncConfig = new AirportSyncConfig(args);
        LOGGER.info("initializing AddAirportsSync with args: {}",String.join(" ", airportSyncConfig.toArgs()));
        if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.ADD_MISSING)) {
            GeoNamesService.initialize(airportSyncConfig.getGNUsername());
        }
        executorService = Executors.newFixedThreadPool(airportSyncConfig.getThreadCount());
        VoyagerServiceRegistry.initialize(airportSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airportService = voyagerServiceRegistry.get(AirportService.class);
    }

    private static List<String> fetchIataCodesToProcess() {
        if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.VERIFY_TYPE)) {
            return verifyTypeList();
        }
        if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.ADD_MISSING)) {
            return airportSyncConfig.getIataList();
        }
        throw new RuntimeException(String.format("Sync mode %s not yet implemented",
                airportSyncConfig.getSyncMode().name()));
    }

    private static List<String> verifyTypeList() {
        List<AirportType> processTypeList = airportSyncConfig.getAirportTypeList();
        IataQuery iataQuery = IataQuery.builder().withAirportTypeList(processTypeList).build();
        long start = System.currentTimeMillis();
        Either<ServiceError,List<String>> either = airportService.getIATACodes(iataQuery);
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

    private static AirportSyncConfig initAirportSync(String[] args) {
        AirportSyncConfig airportSyncConfig = new AirportSyncConfig(args);
        Integer maxConcurrentRequests = airportSyncConfig.getThreadCount();
        VoyagerConfig voyagerConfig = airportSyncConfig.getVoyagerConfig();
        VoyagerServiceRegistry.initialize(voyagerConfig);
        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        airportService = VoyagerServiceRegistry.getInstance().get(AirportService.class);
        return airportSyncConfig;
    }

    private static void processAirports(List<String> iataList) {
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
                if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.VERIFY_TYPE)) {
                    if (result.isRight()) {
                        processAirportCh(result.get(), skippedMatches, patchesMade);
                    } else {
                        patchAsUnverified(result.getLeft(), skippedMatches, skippedFromChError);
                    }
                } else if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.ADD_MISSING)) {
                    buildAndCreateAirport(result);
                } else throw new RuntimeException(String.format("Sync mode %s not yet implemented",
                        airportSyncConfig.getSyncMode().name()));
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to get future: " + future);
            }
        });
        executorService.shutdown();
    }

    private static void buildAndCreateAirport(Either<String, AirportCH> result) {
        AirportType airportType;
        String iata;
        String name = null;
        String countryCode = null;
        Double latitude = null;
        Double longitude = null;

        if (result.isLeft()) {
            iata = result.getLeft();
            airportType = AirportType.UNVERIFIED;
        } else {
            AirportCH airportCH = result.get();
            iata = airportCH.getIata();
            name = airportCH.getName();
            countryCode= airportCH.getCountryCode();
            airportType = airportCH.getType();
            latitude = airportCH.getLatitude();
            longitude = airportCH.getLongitude();
        }

        String city = null;
        String zoneIdString = null;
        Either<ServiceError, Option<AirportDetailsFR>> airportFREither = FlightRadarService.fetchAirportDetails(iata);
        if (airportFREither.isRight() && airportFREither.get().isDefined()) {
            AirportDetailsFR airportDetailsFR = airportFREither.get().get();
            if (StringUtils.isBlank(name)) name = airportDetailsFR.getDetails().getName();
            if (latitude == null) latitude = airportDetailsFR.getDetails().getPosition().getLatitude();
            if (longitude == null) longitude = airportDetailsFR.getDetails().getPosition().getLongitude();
            city = airportDetailsFR.getDetails().getPosition().getRegion().getCity();
            zoneIdString = airportDetailsFR.getDetails().getTimezone().getZoneId();
        }

        if (longitude == null || latitude == null) {
            LOGGER.error("longitude and latitude for {} missing, skipping",iata);
            return;
        }

        String subdivision = null;
        Either<ServiceError, List<GeoName>> geoNameEither = GeoNamesService.findNearbyPlaces(latitude,longitude);
        if (geoNameEither.isRight() && !geoNameEither.get().isEmpty()) {
            GeoName geoName = geoNameEither.get().get(0);
            if (StringUtils.isBlank(city)) city = geoName.getName();
            if (StringUtils.isBlank(countryCode)) countryCode = geoName.getCountryCode();
            subdivision = geoName.getAdminName1();
        }

        if (StringUtils.isBlank(zoneIdString)) {
            Either<ServiceError, Timezone> either = GeoNamesService.getTimezone(latitude,longitude);
            if (either.isRight()) {
                zoneIdString = either.get().getTimezoneId();
            }
        }

        AirportForm airportForm = AirportForm.builder().iata(iata).airportType(airportType.name())
                .zoneId(zoneIdString).subdivision(subdivision).countryCode(countryCode).subdivision(subdivision)
                .latitude(String.valueOf(latitude)).longitude(String.valueOf(longitude)).name(name).city(city)
                .build();

        if (StringUtils.isBlank(airportForm.getName()) || StringUtils.isBlank(airportForm.getCountryCode())
                || StringUtils.isBlank(airportForm.getSubdivision()) || StringUtils.isBlank(airportForm.getZoneId())) {
            LOGGER.error("required data missing from airport form {}, skipping create",airportForm);
            return;
        }
        LOGGER.info("adding airport {}",airportForm);
        Either<ServiceError, Airport> either = airportService.createAirport(airportForm);
        if (either.isLeft()) {
            LOGGER.error("failed to create airport {}, returned service error: {}",iata,either.getLeft().getException().getMessage());
        } else {
            LOGGER.info("succcesfully created airport {}",either.get());
        }
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
}
