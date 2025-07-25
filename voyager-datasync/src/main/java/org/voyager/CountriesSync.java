package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.model.geoname.CountryGN;
import org.voyager.model.geoname.GeoNameFull;
import org.voyager.service.CountryService;
import org.voyager.service.GeoNamesService;
import org.voyager.service.Voyager;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CountriesSync {
    private static CountryService countryService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CountriesSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from countries sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);

        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        Voyager voyager = new Voyager(voyagerConfig);
        countryService = voyager.getCountryService();

        // load countries from files/fetch from urls
        Either<ServiceError, List<CountryGN>> geoCountriesEither = GeoNamesService.getCountryGNList();
        if (geoCountriesEither.isLeft()) {
            Exception exception = geoCountriesEither.getLeft().getException();
            LOGGER.error(String.format("Failed to get countries from GeoNames, error: %s",exception.getMessage()),exception);
            return;
        }
        Either<ServiceError, List<Country>> voyagerCountriesEither = countryService.getCountries();
        if (voyagerCountriesEither.isLeft()) {
            Exception exception = voyagerCountriesEither.getLeft().getException();
            LOGGER.error(String.format("Failed to get countries from Voyager API, error: %s",exception.getMessage()),exception);
            return;
        }
        // load countries from API
        List<Country> countryList = voyagerCountriesEither.get();
        Set<String> countryCodes = countryList.stream().map(Country::getCode).collect(Collectors.toSet());
        List<CountryForm> countryFormList = new ArrayList<>();
        Map<CountryGN,CompletableFuture<Either<ServiceError,GeoNameFull>>> completableFutureList = new HashMap<>();
        geoCountriesEither.get().stream()
                .filter(countryGN -> !countryCodes.contains(countryGN.getCountryCode()))
                .forEach(countryGN -> completableFutureList.put(countryGN,
                        CompletableFuture.supplyAsync(()->GeoNamesService.fetchFull(countryGN.getGeonameId())))
                );
        List<CountryGN> failedCountryGNList = new ArrayList<>();
        completableFutureList.forEach((countryGN,cf) -> {
            try {
                Either<ServiceError, GeoNameFull> either = cf.get();
                if (either.isLeft()) {
                    Exception exception = either.getLeft().getException();
                    LOGGER.error(exception.getMessage(),exception);
                    failedCountryGNList.add(countryGN);
                    return;
                }
                GeoNameFull geoNameFull = either.get();
                countryFormList.add(CountryForm.builder()
                        .countryCode(countryGN.getCountryCode())
                        .countryName(countryGN.getCountryName())
                        .capitalCity(countryGN.getCapital())
                        .languages(Arrays.stream(countryGN.getLanguages().split(",")).toList())
                        .continent(Continent.fromDisplayText(countryGN.getContinentName()).name())
                        .population(Long.parseLong(countryGN.getPopulation()))
                        .currencyCode(countryGN.getCurrencyCode())
                        .areaInSqKm(Double.parseDouble(countryGN.getAreaInSqKm()))
                        .west(geoNameFull.getBoundingBox().getWest())
                        .south(geoNameFull.getBoundingBox().getSouth())
                        .east(geoNameFull.getBoundingBox().getEast())
                        .north(geoNameFull.getBoundingBox().getNorth())
                        .build());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.getMessage(),e);
                failedCountryGNList.add(countryGN);
                return;
            }
        });
        Integer skippedExisting = !countryCodes.isEmpty() ? countryCodes.size() - countryFormList.size() : 0;
        AtomicInteger created = new AtomicInteger(0);
        AtomicReference<List<CountryForm>> failedForms = new AtomicReference<>(new ArrayList<>());
        List<CountryForm> toProcess = countryFormList.stream().limit(processLimit).toList();
        LOGGER.info(String.format("Processing %d country forms", toProcess.size()));
        toProcess.forEach(countryForm -> processCountry(countryForm,created,failedForms));
        failedForms.get().forEach(countryForm ->
                LOGGER.error(String.format("failed country form: %s",countryForm)));
        LOGGER.info(String.format("created %d countries, skipped %d existing, %d processed, %d failed creates",
                created.get(),skippedExisting,toProcess.size(),failedForms.get().size()));
    }

    private static void processCountry(CountryForm countryForm, AtomicInteger created,
                                       AtomicReference<List<CountryForm>> failedForms) {
        Either<ServiceError,Country> either = countryService.addCountry(countryForm);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            LOGGER.error(String.format("Failed to add country from form: %s, error: %s",
                    countryForm,exception.getMessage()),exception);
            failedForms.getAndUpdate(countryForms -> {
                countryForms.add(countryForm);
                return countryForms;
            });
            return;
        }
        LOGGER.info(String.format("successfully created %s",either.get()));
        created.getAndIncrement();
    }

}
