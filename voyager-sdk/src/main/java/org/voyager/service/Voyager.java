package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpClient;
import org.voyager.http.VoyagerHttpFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Voyager {
    private static VoyagerHttpFactory voyagerHttpFactory;
    private static VoyagerConfig voyagerConfig;
    private static AirportService airportService;
    private static RouteService routeService;
    private static SearchService searchService;
    private static PathService pathService;
    private static LocationService locationService;
    private static FlightService flightService;
    private static CountryService countryService;
    private static CurrencyService currencyService;
    private static LanguageService languageService;
    private static final Logger LOGGER = LoggerFactory.getLogger(Voyager.class);
    private static final ObjectMapper om = new ObjectMapper();

    public Voyager(VoyagerConfig voyagerConfig) {
        this.voyagerConfig = voyagerConfig;
        this.voyagerHttpFactory = new VoyagerHttpFactory(voyagerConfig.getAuthorizationToken());
    }

    public CountryService getCountryService() {
        if (countryService == null) countryService = new CountryService(voyagerConfig);
        return countryService;
    }

    public CurrencyService getCurrencyService() {
        if (currencyService == null) currencyService = new CurrencyService(voyagerConfig);
        return currencyService;
    }

    public LanguageService getLanguageService() {
        if (languageService == null) languageService = new LanguageService(voyagerConfig);
        return languageService;
    }

    public FlightService getFlightService() {
        if (flightService == null) flightService = new FlightService(voyagerConfig);
        return flightService;
    }

    public LocationService getLocationService() {
        if (locationService == null) locationService = new LocationService(voyagerConfig);
        return locationService;
    }

    public AirportService getAirportService() {
        if (airportService == null) airportService = new AirportService(voyagerConfig);
        return airportService;
    }

    public RouteService getRouteService() {
        if (routeService == null) routeService = new RouteService(voyagerConfig);
        return routeService;
    }

    public SearchService getSearchService() {
        if (searchService == null) searchService = new SearchService(voyagerConfig);
        return searchService;
    }

    public PathService getPathService() {
        if (pathService == null) pathService = new PathService(voyagerConfig);
        return pathService;
    }

    static  <T> Either<ServiceError, T> fetch(String requestURL,HttpMethod httpMethod,TypeReference<T> typeReference) {
        try {
            URI uri = new URI(requestURL);
            HttpRequest request = voyagerHttpFactory.request(uri,httpMethod);
            VoyagerHttpClient client = voyagerHttpFactory.getClient();
            Either<ServiceError, HttpResponse<String>> responseEither = client.send(request);
            if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
            return ServiceUtils.extractMappedResponse(responseEither.get(),typeReference,requestURL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    static Either<ServiceError,Boolean> fetchNoResponseBody(String requestURL, HttpMethod httpMethod) {
        return fetchRequest(requestURL,httpMethod).flatMap(httpResponse ->
                ServiceUtils.confirmValidResponse(httpResponse,requestURL));
    }

    static <T> Either<ServiceError, T> fetch(String requestURL,HttpMethod httpMethod,Class<T> responseType) {
        return fetchRequest(requestURL,httpMethod).flatMap(httpResponse ->
                ServiceUtils.extractMappedResponse(httpResponse,responseType,requestURL));
    }

    static <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod, Class<T> responseType, Object requestBody) {
        try {
            URI uri = new URI(requestURL);
            String jsonPayload = om.writeValueAsString(requestBody);
            HttpRequest request = voyagerHttpFactory.request(uri,httpMethod,jsonPayload);
            Either<ServiceError, HttpResponse<String>> responseEither = voyagerHttpFactory.getClient().send(request);
            if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
            return ServiceUtils.extractMappedResponse(responseEither.get(),responseType,requestURL);
        } catch (URISyntaxException e) {
            String message = String.format("Exception thrown when building URI of %s",requestURL);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (JsonProcessingException e) {
            String message = String.format("Exception thrown when writing json payload of request body %s",requestBody);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    private static Either<ServiceError,HttpResponse<String>> fetchRequest(String requestURL, HttpMethod httpMethod) {
        try {
            URI uri = new URI(requestURL);
            HttpRequest request = voyagerHttpFactory.request(uri,httpMethod);
            return voyagerHttpFactory.getClient().send(request);
        } catch (URISyntaxException e) {
            String message = String.format("Exception thrown when building URI of %s",requestURL);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }
}
