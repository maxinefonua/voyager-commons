package org.voyager.sync;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sync.config.AirportSyncConfig;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.IataQuery;
import org.voyager.sync.model.chaviation.AirportCH;
import org.voyager.sync.model.flightradar.airport.AirportDetailsFR;
import org.voyager.sync.model.flightradar.airport.DetailsFR;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import org.voyager.sync.service.ChAviationService;
import org.voyager.sync.service.FlightRadarService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class AirportSync {
    private static AirportService airportService;
    private static GeoService geoService;
    private static ExecutorService executorService;
    private static AirportSyncConfig airportSyncConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportSync.class);

    static class Task {
        String iata;
        int result;

        Task(String iata, int result) {
            this.iata = iata;
            this.result = result;
        }
    }


    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        long startTime = System.currentTimeMillis();
        init(args);
        List<String> iataList = fetchIataCodesToProcess();
        processAirportsWithCompletionService(iataList);
        long duration = System.currentTimeMillis() - startTime;
        int sec = (int) (duration/1000);
        int min = sec/60;
        sec %= 60;
        int hr = min/60;
        min %= 60;
        LOGGER.info("completed job in {}hr(s) {}min {}sec",hr,min,sec);
    }

    public static void init(String[] args) {
        airportSyncConfig = new AirportSyncConfig(args);
        LOGGER.info("initializing AddAirportsSync with args: {}",String.join(" ", airportSyncConfig.toArgs()));
        executorService = Executors.newFixedThreadPool(airportSyncConfig.getThreadCount());
        VoyagerServiceRegistry.initialize(airportSyncConfig.getVoyagerConfig());
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        airportService = voyagerServiceRegistry.get(AirportService.class);
        geoService = voyagerServiceRegistry.get(GeoService.class);
    }

    private static List<String> fetchIataCodesToProcess() {
        if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.FULL_SYNC)) {
            return fullSyncList();
        }
        if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.ADD_MISSING)) {
            return airportSyncConfig.getIataList();
        }
        throw new RuntimeException(String.format("Sync mode %s not yet implemented",
                airportSyncConfig.getSyncMode().name()));
    }

    private static List<String> fullSyncList() {
        List<AirportType> processTypeList = airportSyncConfig.getAirportTypeList();
        IataQuery iataQuery = IataQuery.builder().withAirportTypeList(processTypeList).build();
        long start = System.currentTimeMillis();
        Either<ServiceError,List<String>> either = airportService.getIATACodes(iataQuery);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }
        LOGGER.info("{} ms elapsed while fetching airports of type {} from Voyager",
                (System.currentTimeMillis()-start),processTypeList);
        List<String> iataCodes = either.get();
        if (processTypeList.contains(AirportType.CIVIL) && iataCodes.size() < 5000) {
            throw new RuntimeException("List of airports from Voyager has a size of %d. " +
                    "Requires investigation or reload of airports prior to processing");
        }
        return iataCodes;
    }

    private static void processAirportsWithCompletionService(List<String> iataList) {

        List<Future<Task>> futureList = new ArrayList<>();
        CompletionService<Task> completionService = new ExecutorCompletionService<>(executorService);
        iataList.forEach(iata -> {
            Callable<Task> task = () -> buildAndPatchOrCreateAirport(iata);
            futureList.add(completionService.submit(task));
        });

        int totalTasks = futureList.size();
        int completedTasks = 0;
        int processingErrors = 0;
        int patches = 0;
        int skips = 0;
        int fails = 0;
        List<String> failedAirportCodes = new ArrayList<>();

        while (completedTasks < totalTasks) {
            try {
                Future<Task> future = completionService.take();
                Task task = future.get();
                completedTasks++;
                if (task.result < 0) {
                    failedAirportCodes.add(task.iata);
                    fails++;
                    LOGGER.debug("task {}/{} for code {} failed",completedTasks,totalTasks,task.iata);
                } else if (task.result == 0) {
                    skips++;
                    LOGGER.debug("task {}/{} for code {} skipped for matching",completedTasks,totalTasks,task.iata);
                } else {
                    patches++;
                    LOGGER.debug("task {}/{} for code {} successfully patched",completedTasks,totalTasks,task.iata);
                }
            } catch (InterruptedException | ExecutionException e) {
                completedTasks++;
                processingErrors++;
                LOGGER.error("task {}/{} failed with error: {}",completedTasks,totalTasks,e.getMessage());
            }
        }
        LOGGER.info("completed {}/{} tasks, {} patches, {} skips, {} failures, {} processing errors",
                completedTasks,totalTasks,patches,skips,fails,processingErrors);
        failedAirportCodes.forEach(LOGGER::error);
        executorService.shutdown();
    }

    private static Task buildAndPatchOrCreateAirport(String iata) {
        Either<ServiceError, AirportCH> chEither = ChAviationService.getAirportCH(iata);
        Either<ServiceError, Airport> fetchEither = airportService.getAirport(iata);
        Either<ServiceError, Option<AirportDetailsFR>> detailsFREither =
                FlightRadarService.fetchAirportDetails(iata);




        if (fetchEither.isLeft()) {
            ServiceError serviceError = fetchEither.getLeft();
            if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.ADD_MISSING)
                    && serviceError.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                return addMissingAirport(iata,chEither,detailsFREither);
            }
            if (airportSyncConfig.getSyncMode().equals(AirportSyncConfig.SyncMode.FULL_SYNC)) {
                return new Task(iata, -1);
            } else  {
                throw new RuntimeException(String.format("Sync mode %s not yet implemented",
                        airportSyncConfig.getSyncMode()));
            }
        }

        Airport existing = fetchEither.get();

        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(existing.getLatitude())
                .longitude(existing.getLongitude()).build();
        Either<ServiceError,List<GeoPlace>> geoEither = geoService.findNearbyPlaces(geoNearbyQuery);

        AirportPatch airportPatch = null;
        if (chEither.isLeft()) {
            if (!existing.getType().equals(AirportType.UNVERIFIED)) {
                airportPatch = new AirportPatch();
                airportPatch.setType(AirportType.UNVERIFIED.name());
            }
        } else {
            if (!existing.getType().equals(chEither.get().getType())) {
                airportPatch = new AirportPatch();
                airportPatch.setType(chEither.get().getType().name());
            }
        }

        if (geoEither.isRight() && !geoEither.get().isEmpty()) {
            GeoPlace geoPlace = geoEither.get().get(0);
            if (StringUtils.isBlank(existing.getCity()) && StringUtils.isNotBlank(geoPlace.getName())) {
                if (airportPatch == null) airportPatch = new AirportPatch();
                airportPatch.setCity(geoPlace.getName());
            }
            if (StringUtils.isBlank(existing.getSubdivision()) && StringUtils.isNotBlank(geoPlace.getAdminName1())) {
                if (airportPatch == null) airportPatch = new AirportPatch();
                airportPatch.setSubdivision(geoPlace.getAdminName1());
            }
        }

        if (detailsFREither.isRight() && detailsFREither.get().isDefined()) {
            AirportDetailsFR airportDetailsFR = detailsFREither.get().get();
            if (StringUtils.isBlank(existing.getCity())
                    && StringUtils.isNotBlank(airportDetailsFR.getDetails().getPosition().getRegion().getCity())) {
                if (airportPatch == null) airportPatch = new AirportPatch();
                if (StringUtils.isBlank(airportPatch.getCity())) {
                    airportPatch.setCity(airportDetailsFR.getDetails()
                            .getPosition().getRegion().getCity());
                }
            }
            if (StringUtils.isBlank(existing.getName())
                    && StringUtils.isNotBlank(airportDetailsFR.getDetails().getName())) {
                if (airportPatch == null) airportPatch = new AirportPatch();
                airportPatch.setName(airportDetailsFR.getDetails().getName());
            }
        }

        if (airportPatch == null) return new Task(iata,0);
        if (StringUtils.isNotBlank(airportPatch.getCity())) {
            airportPatch.setCity(airportPatch.getCity().trim());
        }
        if (StringUtils.isNotBlank(airportPatch.getName())) {
            airportPatch.setName(airportPatch.getName().trim());
        }
        if (StringUtils.isNotBlank(airportPatch.getSubdivision())) {
            airportPatch.setSubdivision(airportPatch.getSubdivision().trim());
        }
        Either<ServiceError, Airport> patchEither = airportService.patchAirport(iata,airportPatch);
        if (patchEither.isLeft()) {
            LOGGER.error("failed to patch {} airport with: {}",iata,airportPatch);
            return new Task(iata,-1);
        }
        LOGGER.info("successfully patched {}",patchEither.get());
        return new Task(iata,1);
    }

    private static Task addMissingAirport(String iata, Either<ServiceError, AirportCH> chEither,
                                          Either<ServiceError, Option<AirportDetailsFR>> detailsFREither) {

        Double latitude = null;
        Double longitude = null;
        String name = null;
        String countryCode = null;
        AirportType airportType = AirportType.UNVERIFIED;

        if (chEither.isRight()) {
            AirportCH airportCH = chEither.get();
            name = airportCH.getName();
            countryCode = airportCH.getCountryCode();
            latitude = airportCH.getLatitude();
            longitude = airportCH.getLongitude();
            airportType = airportCH.getType();
        }

        String city = null;
        String zoneId = null;
        if (detailsFREither.isRight() && detailsFREither.get().isDefined()) {
            DetailsFR detailsFR = detailsFREither.get().get().getDetails();
            if (StringUtils.isBlank(name)) name = detailsFR.getName();
            if (StringUtils.isBlank(countryCode)) countryCode = detailsFR.getPosition().getCountry().getCode();
            if (latitude == null) latitude = detailsFR.getPosition().getLatitude();
            if (longitude == null) longitude = detailsFR.getPosition().getLongitude();
            city = detailsFR.getPosition().getRegion().getCity();
            zoneId = detailsFR.getTimezone().getZoneId();
        }

        if (latitude == null || longitude == null) return new Task(iata,-1);

        String subdivision = null;
        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(latitude).longitude(longitude).build();
        Either<ServiceError, List<GeoPlace>> geoEither = geoService.findNearbyPlaces(geoNearbyQuery);
        if (geoEither.isRight() && !geoEither.get().isEmpty()) {
            GeoPlace geoPlace = geoEither.get().get(0);
            if (StringUtils.isBlank(city)) city = geoPlace.getName();
            if (StringUtils.isBlank(countryCode)) countryCode = geoPlace.getCountryCode();
            subdivision = geoPlace.getAdminName1();
        }

        if (StringUtils.isBlank(zoneId)) {
            GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().latitude(latitude).longitude(longitude).build();
            Either<ServiceError, GeoTimezone> timezoneEither = geoService.getTimezone(geoTimezoneQuery);
            if (timezoneEither.isRight()) {
                zoneId = timezoneEither.get().getTimezoneId();
            }
        }

        if (StringUtils.isNotBlank(subdivision)) subdivision = subdivision.trim();
        if (StringUtils.isNotBlank(name)) name = name.trim();
        if (StringUtils.isNotBlank(city)) city = city.trim();

        AirportForm airportForm = AirportForm.builder().iata(iata).longitude(String.valueOf(longitude))
                .latitude(String.valueOf(latitude)).airportType(airportType.name()).zoneId(zoneId)
                .countryCode(countryCode).subdivision(subdivision).city(city).name(name)
                .build();
        Either<ServiceError,Airport> createEither = airportService.createAirport(airportForm);
        if (createEither.isLeft()) {
            LOGGER.error("{} failed to create {} with error: {}",
                    iata,airportForm,createEither.getLeft().getException().getMessage());
            return new Task(iata,-1);
        } else {
            return new Task(iata,1);
        }
    }
}
