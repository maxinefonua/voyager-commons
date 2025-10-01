package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.flightRadar.RouteFR;
import org.voyager.model.flightRadar.search.AirportScheduleFR;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.voyager.utils.HttpRequestUtils.fetchDocumentFromURL;

public class FlightRadarService {
    private static final ObjectMapper om = new ObjectMapper();

    private static final String AUTH_HEADER = "authority";
    private static final String AUTH_VALUE = "www.flightradar24.com";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightRadarService.class);

    private static final String baseURL = "https://www.flightradar24.com";
    private static final String routesPath = "/data/airlines/%s/routes";
    private static final String airportParam = "?get-airport-arr-dep=%s&format=json";

    public static Either<ServiceError, List<RouteFR>> extractAirlineRoutes(Airline airline) {
        Either<ServiceError, Document> either = fetchDocumentFromURL(baseURL.concat(String.format(routesPath, airline.getPathVariableFR())));
        if (either.isLeft()) return Either.left(either.getLeft());
        Document document = either.get();
        return extractRoutesFromDocument(document);
    }

    public static Either<ServiceError, Option<AirportScheduleFR>> extractAirportResponse(String iata, Airline airline) {
        String requestURL = baseURL.concat(String.format(routesPath, airline.getPathVariableFR()))
                .concat(String.format(airportParam, iata));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(requestURL))
                    .headers(AUTH_HEADER, AUTH_VALUE)
                    .timeout(Duration.ofSeconds(60))
                    .method(HttpMethod.GET.name(), HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                int maxRetries = 3;
                int attempt = 0;
                while (attempt < maxRetries) {
                    attempt++;
                    long delay = 1000L;
                    Optional<String> retryValue = response.headers().firstValue("Retry-After");
                    if (retryValue.isPresent())
                        delay = Integer.parseInt(retryValue.get()) * 1000L;
                    LOGGER.info(String.format("will attempt %d/%d retires after %dms to %s",
                            attempt, maxRetries, delay, request.uri().toString()));
                    Thread.sleep(delay);
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 429) break;
                }
            }
            if (response.statusCode() == 429) {
                return Either.left(new ServiceError(HttpStatus.TOO_MANY_REQUESTS, new ServiceException(
                        String.format("Too Many Requests returned after Max Retries from endpoint [%s]\nStatus: %d\nResponse: %s",
                                requestURL, response.statusCode(), response.body()))));
            }
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, new ServiceException(
                        String.format("Non-200 status returned from endpoint [%s]\nStatus: %d\nResponse: %s",
                                requestURL, response.statusCode(), response.body()))));
            }
            return convertResponseBody(response.body(),iata,airline);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e));
        }
    }

    private static Either<ServiceError,List<RouteFR>> extractRoutesFromDocument(Document document) {
        Element element = document.select("script").stream().filter(script -> script.html().contains("var arrRoutes=")).findFirst().orElse(null);
        if (element != null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("graal.js");
            try {
                engine.eval(element.html());
                Object arrRoutes = engine.get("arrRoutes");
                if (arrRoutes == null) {
                    String errorMessage = "'arrRoutes' returned null from script.";
                    return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                            new ServiceException(errorMessage)));
                }
                return convertRoutesObjectToRoutesJson(arrRoutes);
            } catch (ScriptException e) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format("Error while evaluating script. Message: %s. Script: %s",
                                e.getMessage(), element.html()),e));
            }
        } else {
            String errorMessage = "Error selecting script from document. No match for 'var arrRoutes='";
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ServiceException(errorMessage)));
        }
    }

    private static Either<ServiceError,List<RouteFR>> convertRoutesObjectToRoutesJson(Object arrRoutes) {
        ObjectMapper om = new ObjectMapper();
        try {
            String arrRoutesJsonString = om.writeValueAsString(arrRoutes);
            return extractRoutes(arrRoutesJsonString);
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Error while writing object as json string. Message: %s. Object: %s",
                            e.getMessage(), arrRoutes.toString()), e));
        }
    }

    private static Either<ServiceError,List<RouteFR>> extractRoutes(String arrRoutesJsonString) {
        LOGGER.debug(arrRoutesJsonString);
        ObjectMapper om = new ObjectMapper();
        try {
            List<RouteFR> routeFRList = om.readValue(arrRoutesJsonString, new TypeReference<List<RouteFR>>(){});
            return Either.right(routeFRList);
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(),e));
        }
    }

    private static Either<ServiceError, Option<AirportScheduleFR>> convertResponseBody(String jsonBody, String iata, Airline airline) {
        if (StringUtils.isBlank(jsonBody) || jsonBody.equals("[]")) {
            LOGGER.info(String.format("%s airport returns no flights for airline %s",
                    iata, airline.name()));
            return Either.right(Option.none());
        } else {
            try {
                AirportScheduleFR airportScheduleFR = om.readValue(jsonBody, AirportScheduleFR.class);
                return Either.right(Option.of(airportScheduleFR));
            } catch (JsonProcessingException e) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e));
            }
        }
    }
}
