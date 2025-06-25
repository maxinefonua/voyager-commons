package org.voyager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class FlightRadarService {
    private static final ObjectMapper om = new ObjectMapper();

    private static final String AUTH_HEADER = "authority";
    private static final String AUTH_VALUE = "www.flightradar24.com";
    private static final Map<Airline,String> AIRLINE_PATHVAR_MAP = Map.of(
            Airline.DELTA,"dl-dal",
            Airline.JAPAN,"jl-jal",
            Airline.NORWEGIAN,"dy-noz"
    );

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightRadarService.class);

    private static final String baseURL = "https://www.flightradar24.com";
    private static final String routesPath = "/data/airlines/%s/routes";
    private static final String airportParam = "?get-airport-arr-dep=%s&format=json";

    public static Either<ServiceError,String> fetchAirportResponse(String iata, Airline airline) {
        String requestURL = baseURL.concat(String.format(routesPath,AIRLINE_PATHVAR_MAP.get(airline)))
                .concat(String.format(airportParam,iata));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(requestURL))
                    .headers(AUTH_HEADER,AUTH_VALUE)
                    .timeout(Duration.ofSeconds(60))
                    .method(HttpMethod.GET.name(), HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .build();
            HttpResponse<String> response  = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                int maxRetries = 3;
                int attempt = 0;
                while (attempt < maxRetries) {
                    attempt++;
                    long delay = 1000L;
                    Optional<String> retryValue = response.headers().firstValue("Retry-After");
                    if (retryValue.isPresent())
                        delay = Integer.parseInt(retryValue.get())*1000L;
                    LOGGER.info(String.format("will attempt %d/%d retires after %dms to %s",
                            attempt,maxRetries,delay,request.uri().toString()));
                    Thread.sleep(delay);
                    response = client.send(request,HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 429) break;
                }
            }
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, new ServiceException(
                        String.format("Non-200 status returned from endpoint [%s]\nStatus: %d\nResponse: %s",
                                requestURL,response.statusCode(),response.body()))));
            }
            return Either.right(response.body());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(),e));
        }
    }
  }
