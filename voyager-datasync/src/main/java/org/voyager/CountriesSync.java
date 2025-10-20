package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.CountriesSyncConfig;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.model.geoname.CountryGN;
import org.voyager.model.geoname.GeoNameFull;
import org.voyager.model.nominatim.FeatureSearch;
import org.voyager.service.CountryService;
import org.voyager.service.GeoNamesService;
import org.voyager.service.NominatimService;
import org.voyager.service.impl.VoyagerServiceRegistry;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CountriesSync {
    private static CountryService countryService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CountriesSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from countries sync main");
        initCountriesSync(args);
        Set<String> countryCodeSet = loadCountriesFromAPI();
        List<CountryGN> countryGNList = loadCountriesFromGN();
        processCountries(countryCodeSet,countryGNList);
    }

    private static void processCountries(Set<String> countryCodeSet, List<CountryGN> countryGNList) {
        Set<String> nominatimCountryCodes = Set.of("NZ","US");
        List<CountryForm> countryFormList = new ArrayList<>();
        Map<CountryGN,CompletableFuture<Either<ServiceError,Object>>> completableFutureList = new HashMap<>();
        countryGNList.stream()
                .filter(countryGN -> !countryCodeSet.contains(countryGN.getCountryCode()))
                .forEach(countryGN -> {
                    if (nominatimCountryCodes.contains(countryGN.getCountryCode()))
                        completableFutureList.put(countryGN,
                                CompletableFuture.supplyAsync(()-> NominatimService.searchCountryName(countryGN.getCountryName()))
                                        .thenApply(either -> either.map(featureSearch -> featureSearch)));
                    else completableFutureList.put(countryGN,
                            CompletableFuture.supplyAsync(()->GeoNamesService.fetchFull(countryGN.getGeonameId()))
                                    .thenApply(either -> either.map(geoNameFull -> geoNameFull)));
                });
        List<CountryGN> failedCountryGNList = new ArrayList<>();
        completableFutureList.forEach((countryGN,cf) -> {
            processGNCompletableFuture(countryGN,cf,failedCountryGNList,nominatimCountryCodes,countryFormList);
        });
        printResults(countryCodeSet,countryFormList,failedCountryGNList);
    }

    private static void printResults(Set<String> countryCodeSet, List<CountryForm> countryFormList, List<CountryGN> failedCountryGNList) {
        Integer skippedExisting = !countryCodeSet.isEmpty() ? countryCodeSet.size() - countryFormList.size() : 0;
        AtomicInteger created = new AtomicInteger(0);
        AtomicReference<List<CountryForm>> failedForms = new AtomicReference<>(new ArrayList<>());
        List<CountryForm> toProcess = countryFormList.stream().toList();
        LOGGER.info(String.format("Processing %d country forms", toProcess.size()));
        toProcess.forEach(countryForm -> processCountry(countryForm,created,failedForms));
        failedCountryGNList.forEach(countryGN -> LOGGER.error(
                String.format("failed fetching full geoname for %s",countryGN)));
        failedForms.get().forEach(countryForm ->
                LOGGER.error(String.format("failed country form: %s",countryForm)));
        LOGGER.info(String.format("created %d countries, skipped %d existing, %d processed, %d failed creates, %d failed fetch full geoname",
                created.get(),skippedExisting,toProcess.size(),failedForms.get().size(),failedCountryGNList.size()));
    }

    private static void processGNCompletableFuture(CountryGN countryGN,
                                                   CompletableFuture<Either<ServiceError, Object>> cf,
                                                   List<CountryGN> failedCountryGNList,
                                                   Set<String> nominatimCountryCodes,
                                                   List<CountryForm> countryFormList) {
        try {
            Either<ServiceError, Object> either = cf.get();
            if (either.isLeft()) {
                Exception exception = either.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
                failedCountryGNList.add(countryGN);
                return;
            }
            if (nominatimCountryCodes.contains(countryGN.getCountryCode())) {
                FeatureSearch featureSearch = (FeatureSearch) either.get();
                countryFormList.add(CountryForm.builder()
                        .countryCode(countryGN.getCountryCode())
                        .countryName(countryGN.getCountryName())
                        .capitalCity(countryGN.getCapital())
                        .continent(Continent.fromDisplayText(countryGN.getContinentName()).name())
                        .population(Long.parseLong(countryGN.getPopulation()))
                        .currencyCode(countryGN.getCurrencyCode())
                        .areaInSqKm(Double.parseDouble(countryGN.getAreaInSqKm()))
                        .west((countryGN.getCountryCode().equals("NZ"))?
                                Math.abs(featureSearch.getBoundingbox()[2]) : featureSearch.getBoundingbox()[2])
                        .south(featureSearch.getBoundingbox()[0])
                        .east(featureSearch.getBoundingbox()[3])
                        .north(featureSearch.getBoundingbox()[1])
                        .build());
                return;
            }
            GeoNameFull geoNameFull = (GeoNameFull) either.get();
            countryFormList.add(CountryForm.builder()
                    .countryCode(countryGN.getCountryCode())
                    .countryName(countryGN.getCountryName())
                    .capitalCity(countryGN.getCapital())
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
        }
    }

    private static List<CountryGN> loadCountriesFromGN() {
        Either<ServiceError, List<CountryGN>> either = GeoNamesService.getCountryGNList();
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
        GeoNamesService.initialize(countriesSyncConfig.getGNUsername());

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,voyagerAuthorizationToken);
        VoyagerServiceRegistry.initialize(voyagerConfig);
        countryService = VoyagerServiceRegistry.getInstance().get(CountryService.class);
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
