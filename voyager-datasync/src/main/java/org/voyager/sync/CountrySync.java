package org.voyager.sync;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.sync.config.CountriesSyncConfig;
import org.voyager.sdk.config.Protocol;
import org.voyager.sdk.config.VoyagerConfig;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.commons.model.geoname.GeoCountry;
import org.voyager.commons.model.geoname.GeoFull;
import org.voyager.commons.model.nominatim.FeatureSearch;
import org.voyager.sdk.service.CountryService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sync.service.NominatimService;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CountrySync {
    private static CountryService countryService;
    private static GeoService geoService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CountrySync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from countries sync main");
        initCountriesSync(args);
        Set<String> countryCodeSet = loadCountriesFromAPI();
        List<GeoCountry> geoCountryList = loadCountriesFromGN();
        processCountries(countryCodeSet, geoCountryList);
    }

    private static void processCountries(Set<String> countryCodeSet, List<GeoCountry> geoCountryList) {
        Set<String> nominatimCountryCodes = Set.of("NZ","US");
        List<CountryForm> countryFormList = new ArrayList<>();
        Map<GeoCountry,CompletableFuture<Either<ServiceError,Object>>> completableFutureList = new HashMap<>();
        geoCountryList.stream()
                .filter(geoCountry -> !countryCodeSet.contains(geoCountry.getCountryCode()))
                .forEach(geoCountry -> {
                    if (nominatimCountryCodes.contains(geoCountry.getCountryCode()))
                        completableFutureList.put(geoCountry,
                                CompletableFuture.supplyAsync(()-> NominatimService.searchCountryName(geoCountry.getCountryName()))
                                        .thenApply(either -> either.map(featureSearch -> featureSearch)));
                    else completableFutureList.put(geoCountry,
                            CompletableFuture.supplyAsync(()-> geoService.getFull(geoCountry.getGeonameId()))
                                    .thenApply(either -> either.map(geoNameFull -> geoNameFull)));
                });
        List<GeoCountry> failedGeoCountryList = new ArrayList<>();
        completableFutureList.forEach((geoCountry, cf) ->
                processGNCompletableFuture(geoCountry,cf, failedGeoCountryList,nominatimCountryCodes,countryFormList));
        printResults(countryCodeSet,countryFormList, failedGeoCountryList);
    }

    private static void printResults(Set<String> countryCodeSet, List<CountryForm> countryFormList, List<GeoCountry> failedGeoCountryList) {
        Integer skippedExisting = !countryCodeSet.isEmpty() ? countryCodeSet.size() - countryFormList.size() : 0;
        AtomicInteger created = new AtomicInteger(0);
        AtomicReference<List<CountryForm>> failedForms = new AtomicReference<>(new ArrayList<>());
        List<CountryForm> toProcess = countryFormList.stream().toList();
        LOGGER.info("Processing {} country forms", toProcess.size());
        toProcess.forEach(countryForm -> processCountry(countryForm,created,failedForms));
        failedGeoCountryList.forEach(geoCountry ->
                LOGGER.error("failed fetching full geoname for {}", geoCountry));
        failedForms.get().forEach(countryForm -> LOGGER.error("failed country form: {}", countryForm));
        LOGGER.info("created {} countries, skipped {} existing, {} processed, {} failed creates, {} failed fetch full geoname",
                created.get(), skippedExisting, toProcess.size(), failedForms.get().size(),
                failedGeoCountryList.size());
    }

    private static void processGNCompletableFuture(GeoCountry geoCountry,
                                                   CompletableFuture<Either<ServiceError, Object>> cf,
                                                   List<GeoCountry> failedGeoCountryList,
                                                   Set<String> nominatimCountryCodes,
                                                   List<CountryForm> countryFormList) {
        try {
            Either<ServiceError, Object> either = cf.get();
            if (either.isLeft()) {
                Exception exception = either.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                failedGeoCountryList.add(geoCountry);
                return;
            }
            if (nominatimCountryCodes.contains(geoCountry.getCountryCode())) {
                FeatureSearch featureSearch = (FeatureSearch) either.get();
                countryFormList.add(CountryForm.builder()
                        .countryCode(geoCountry.getCountryCode())
                        .countryName(geoCountry.getCountryName())
                        .capitalCity(geoCountry.getCapital())
                        .continent(Continent.fromDisplayText(geoCountry.getContinentName()).name())
                        .population(Long.parseLong(geoCountry.getPopulation()))
                        .currencyCode(geoCountry.getCurrencyCode())
                        .areaInSqKm(Double.parseDouble(geoCountry.getAreaInSqKm()))
                        .west((geoCountry.getCountryCode().equals("NZ"))?
                                Math.abs(featureSearch.getBoundingbox()[2]) : featureSearch.getBoundingbox()[2])
                        .south(featureSearch.getBoundingbox()[0])
                        .east(featureSearch.getBoundingbox()[3])
                        .north(featureSearch.getBoundingbox()[1])
                        .build());
                return;
            }
            GeoFull geoNameFull = (GeoFull) either.get();
            countryFormList.add(CountryForm.builder()
                    .countryCode(geoCountry.getCountryCode())
                    .countryName(geoCountry.getCountryName())
                    .capitalCity(geoCountry.getCapital())
                    .continent(Continent.fromDisplayText(geoCountry.getContinentName()).name())
                    .population(Long.parseLong(geoCountry.getPopulation()))
                    .currencyCode(geoCountry.getCurrencyCode())
                    .areaInSqKm(Double.parseDouble(geoCountry.getAreaInSqKm()))
                    .west(geoNameFull.getBoundingBox().getWest())
                    .south(geoNameFull.getBoundingBox().getSouth())
                    .east(geoNameFull.getBoundingBox().getEast())
                    .north(geoNameFull.getBoundingBox().getNorth())
                    .build());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(),e);
            failedGeoCountryList.add(geoCountry);
        }
    }

    private static List<GeoCountry> loadCountriesFromGN() {
        Either<ServiceError, List<GeoCountry>> either = geoService.getCountries();
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(String.format("Failed to get countries from GeoNames, error: %s",
                    exception.getMessage()),exception);
        }
        return either.get();
    }

    private static Set<String> loadCountriesFromAPI() {
        Either<ServiceError, List<Country>> voyagerCountriesEither = countryService.getCountries();
        if (voyagerCountriesEither.isLeft()) {
            Exception exception = voyagerCountriesEither.getLeft().getException();
            throw new RuntimeException(String.format("Failed to get countries from Voyager API, error: %s",exception.getMessage()),exception);
        }
        // load countries from API
        List<Country> countryList = voyagerCountriesEither.get();
        return countryList.stream().map(Country::getCode).collect(Collectors.toSet());
    }

    private static void initCountriesSync(String[] args) {
        CountriesSyncConfig countriesSyncConfig = new CountriesSyncConfig(args);
        String host = countriesSyncConfig.getHostname();
        int port = countriesSyncConfig.getPort();
        String voyagerAuthorizationToken = countriesSyncConfig.getAccessToken();
        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,voyagerAuthorizationToken);
        VoyagerServiceRegistry.initialize(voyagerConfig);
        VoyagerServiceRegistry voyagerServiceRegistry = VoyagerServiceRegistry.getInstance();
        countryService = voyagerServiceRegistry.get(CountryService.class);
        geoService = voyagerServiceRegistry.get(GeoService.class);
    }

    private static void processCountry(CountryForm countryForm, AtomicInteger created,
                                       AtomicReference<List<CountryForm>> failedForms) {
        Either<ServiceError,Country> either = countryService.addCountry(countryForm);
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            LOGGER.error("Failed to add country from form: {}, error: {}",
                    countryForm, exception.getMessage(), exception);
            failedForms.getAndUpdate(countryForms -> {
                countryForms.add(countryForm);
                return countryForms;
            });
            return;
        }
        LOGGER.info("successfully created {}", either.get());
        created.getAndIncrement();
    }

}
