package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.geoname.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GeoNamesService {
    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient CLIENT =  HttpClient.newHttpClient();

    private static final String baseURL = "https://secure.geonames.org/";
    private static final String USERNAME = System.getenv("GEONAMES_USERNAME");

    private static final String nearbyPlacePath = "/findNearbyPlaceNameJSON";
    private static final String nearbyPlaceParams = "?username=%s&lat=%f&lng=%f";

    private static final String timezonePath = "/timezoneJSON";
    private static final String timezoneParams = "?lat=%f&lng=%f&username=%s";

    private static final String countryPath = "/countryInfoJSON";
    private static final String countryParams = "?username=%s";

    private static final String getPath = "/getJSON";
    private static final String getParams = "?geonameId=%d&username=%s";


    public static Either<ServiceError, List<GeoName>> findNearbyPlaces(Double latitude, Double longitude) {
        String requestURL = baseURL.concat(nearbyPlacePath).concat(String.format(nearbyPlaceParams,USERNAME,latitude,longitude));
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        new ServiceException(String.format("Non-200 response from requestURL %s, status: %d, body: %s",
                                requestURL,response.statusCode(),response.body()))));
            }
            String jsonBody = response.body();
            GeoNameResponse geoNameResponse = om.readValue(jsonBody, GeoNameResponse.class);
            return Either.right(geoNameResponse.getGeonames());
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    public static Either<ServiceError, Timezone> getTimezone(Double latitude, Double longitude) {
        String requestURL = baseURL.concat(timezonePath).concat(String.format(timezoneParams,latitude,longitude,USERNAME));
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        new ServiceException(String.format("Non-200 response from requestURL %s, status: %d, body: %s",
                                requestURL,response.statusCode(),response.body()))));
            }
            String jsonBody = response.body();
            Timezone timezone = om.readValue(jsonBody, Timezone.class);
            return Either.right(timezone);
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    public static Either<ServiceError,GeoNameFull> fetchFull(@NonNull Long geonameId) {
        String requestURL = baseURL.concat(getPath).concat(String.format(getParams,geonameId,USERNAME));
        return getRequestBody(requestURL,GeoNameFull.class);
    }

    public static Either<ServiceError,List<CountryGN>> getCountryGNList() {
        String requestURL = baseURL.concat(countryPath).concat(String.format(countryParams,USERNAME));
        return getRequestBody(requestURL,CountryGNResponse.class).map(CountryGNResponse::getCountryGNList);
    }

    private static <T> Either<ServiceError,T> getRequestBody(String requestURL, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        new ServiceException(String.format("Non-200 response from requestURL %s, status: %d, body: %s",
                                requestURL,response.statusCode(),response.body()))));
            }
            String jsonBody = response.body();
            return extractResponseClass(jsonBody, responseType);
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
       } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    private static <T> Either<ServiceError,T> extractResponseClass(String jsonBody, Class<T> responseType) {
        try {
            return Either.right(om.readValue(jsonBody,responseType));
        } catch (JsonProcessingException e)  {
            String message = String.format("error reading json response body: %s to class: %s, error: %s",jsonBody,responseType.getSimpleName(),e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }
}
