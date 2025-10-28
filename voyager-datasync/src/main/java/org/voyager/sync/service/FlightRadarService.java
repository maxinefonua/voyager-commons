package org.voyager.sync.service;

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
import org.voyager.sync.config.external.FlightRadarConfig;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sync.model.flightradar.RouteFR;
import org.voyager.sync.model.flightradar.airport.AirportDetailsFR;
import org.voyager.sync.model.flightradar.search.AirportScheduleFR;
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
import static org.voyager.sync.utils.HttpRequestUtils.fetchDocumentFromURLWithRetry;

public class FlightRadarService {
    private static final ObjectMapper om = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightRadarService.class);
    private static final FlightRadarConfig flightRadarConfig = new FlightRadarConfig();

    public static Either<ServiceError, List<RouteFR>> extractAirlineRoutes(Airline airline) {
        String requestURL = String.format(flightRadarConfig.getRoutesPathWithParam(),airline.getPathVariableFR());
        Either<ServiceError, Document> either = fetchDocumentFromURLWithRetry(requestURL);
        if (either.isLeft()) return Either.left(either.getLeft());
        Document document = either.get();
        return extractRoutesFromDocument(document,requestURL);
    }

    public static Either<ServiceError, Option<AirportScheduleFR>> extractAirportResponseWithRetry(String airportCode1,
                                                                                                  String airportCode2) {
        String requestURL = String.format(flightRadarConfig.getAirportRoutesPathWithParams(),airportCode1,airportCode2);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(requestURL))
                    .headers(flightRadarConfig.getAuthHeader(), flightRadarConfig.getAuthValue())
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
                    LOGGER.info("will attempt {}/{} retires after {}ms ({}sec) to {}",
                            attempt, maxRetries, delay, delay / 1000, request.uri().toString());
                    Thread.sleep(delay);
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 429) break;
                }
            }
            return processAirportResponse(response,airportCode1,airportCode2,requestURL);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e));
        }
    }

    private static Either<ServiceError, Option<AirportScheduleFR>> processAirportResponse(HttpResponse<String> response,
                                                                                          String airportCode1,
                                                                                          String airportCode2,
                                                                                          String requestURL) {
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
        return convertAirportResponseBody(response.body(), airportCode1,airportCode2,requestURL);
    }

    private static Either<ServiceError,List<RouteFR>> extractRoutesFromDocument(Document document,String requestURL) {
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
            String errorMessage = "no element match for 'var arrRoutes=' at :"+requestURL;
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
        LOGGER.trace(arrRoutesJsonString);
        ObjectMapper om = new ObjectMapper();
        try {
            List<RouteFR> routeFRList = om.readValue(arrRoutesJsonString, new TypeReference<>(){});
            return Either.right(routeFRList);
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(),e));
        }
    }

    private static Either<ServiceError, Option<AirportScheduleFR>> convertAirportResponseBody(String jsonBody,
                                                                                              String airportCode1,
                                                                                              String airportCode2,
                                                                                              String requestURL) {
        if (StringUtils.isBlank(jsonBody) || jsonBody.equals("[]")) {
            LOGGER.info("{} airport returns no flights for airport {}", airportCode1, airportCode2);
            return Either.right(Option.none());
        } else {
            try {
                AirportScheduleFR airportScheduleFR = om.readValue(jsonBody, AirportScheduleFR.class);
                return Either.right(Option.of(airportScheduleFR));
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException after successful fetch of AirportScheduleFR at {}, error: {}",
                        requestURL, e.getMessage());
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e));
            }
        }
    }

    public static Either<ServiceError, Option<AirportDetailsFR>> fetchAirportDetails(String iata) {
        String requestURL = String.format(flightRadarConfig.getAirportDetailsWithParam(),iata);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(requestURL))
                    .headers(flightRadarConfig.getAuthHeader(), flightRadarConfig.getAuthValue())
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
                    LOGGER.info("will attempt {}/{} retires after {}ms ({}sec) to {}",
                            attempt, maxRetries, delay, delay / 1000, request.uri().toString());
                    Thread.sleep(delay);
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 429) break;
                }
            }
            return processAirportDetailsResponse(response,iata,requestURL);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e));
        }
    }

    private static Either<ServiceError, Option<AirportDetailsFR>> processAirportDetailsResponse(HttpResponse<String> response,
                                                                                                String iata,
                                                                                                String requestURL) {
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
        return convertAirportDetailsResponseBody(response.body(),iata,requestURL);
    }

    private static Either<ServiceError, Option<AirportDetailsFR>> convertAirportDetailsResponseBody(String jsonBody,
                                                                                            String iata,
                                                                                            String requestURL) {
        if (StringUtils.isBlank(jsonBody) || jsonBody.equals("[]")) {
            LOGGER.info("{} airport returns no details", iata);
            return Either.right(Option.none());
        } else {
            try {
                AirportDetailsFR airportDetailsFR = om.readValue(jsonBody, AirportDetailsFR.class);
                return Either.right(Option.of(airportDetailsFR));
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException after successful fetch of AirportDetailsFR at {}, error: {}",
                        requestURL, e.getMessage());
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e));
            }
        }
    }
}
