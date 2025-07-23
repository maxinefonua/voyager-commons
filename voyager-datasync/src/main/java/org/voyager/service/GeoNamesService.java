package org.voyager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.geoname.GeoName;
import org.voyager.model.geoname.GeoNameResponse;
import org.voyager.model.geoname.Timezone;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GeoNamesService {
    private static final ObjectMapper om = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesService.class);

    private static final String baseURL = "https://secure.geonames.org/";
    private static final String nearbyPlacePath = "/findNearbyPlaceNameJSON";
    private static final String timezonePath = "/timezoneJSON";
    private static final String nearbyPlaceParams = "?username=%s&lat=%f&lng=%f";
    private static final String timezoneParams = "?lat=%f&lng=%f&username=%s";
    private static final String USERNAME = System.getenv("GEONAMES_USERNAME");

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
}
