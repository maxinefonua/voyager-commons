package org.voyager.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.model.Airline;
import org.voyager.service.impl.VoyagerAPIService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

public abstract class VoyagerAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoyagerAPIService.class);
    @Getter
    private static String baseUrl;
    private static String voyagerAPIKey;

    protected VoyagerAPI() {
        validateEnvironVars(List.of(VOYAGER_API_KEY,VOYAGER_BASE_URL));
        baseUrl = System.getenv(VOYAGER_BASE_URL);
        voyagerAPIKey = System.getenv(VOYAGER_API_KEY);
    }

    protected HttpResponse<String> getResponse(String fullURL) {
        LOGGER.debug(String.format("fullURL: %s",fullURL));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(fullURL))
                    .headers(AUTH_TOKEN_HEADER_NAME,voyagerAPIKey)
                    .GET()
                    .build();
            return HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error(String.format("Error sending http with fullURL: %s. Message: %s",fullURL,e.getMessage()),e);
            throw new RuntimeException(e);
        }
    }

    protected HttpResponse<String> postResponse(String fullURL,String jsonBody) {
        LOGGER.debug(String.format("fullURL: %s",fullURL));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(fullURL))
                    .headers(AUTH_TOKEN_HEADER_NAME,voyagerAPIKey,CONTENT_TYPE_HEADER_NAME,JSON_TYPE_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            return HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error(String.format("Error sending http with fullURL: %s. Message: %s",fullURL,e.getMessage()),e);
            throw new RuntimeException(e);
        }
    }
    public abstract HttpResponse<String> getAirportByIata(String iata);
    public abstract HttpResponse<String> getRoute();
    public abstract HttpResponse<String> getRoute(String origin, String destination, Airline airline);
    public abstract HttpResponse<String> addRoute(String routeJson);
}
