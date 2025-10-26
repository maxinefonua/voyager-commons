package org.voyager.sync.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HttpRequestUtils {
    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtils.class);

    private static Either<ServiceError,Document> getHTMLDocFromURL(String URL, Map<String,String> headers) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET();
        headers.forEach(requestBuilder::setHeader);
        HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()), new ServiceException(
                    String.format("request to '%s' returned %s, response: %s",URL,response.statusCode(), response.body()))));
        }
        return Either.right(Jsoup.parse(response.body()));
    }

    public static Either<ServiceError,Document> fetchDocumentFromURLWithRetry(String routesURL) {
        try {
            return getHTMLDocFromURLWithRetry(routesURL, Map.of());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),e));
        }
    }

    private static Either<ServiceError,Document> getHTMLDocFromURLWithRetry(String URL, Map<String,String> headers)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET();
        headers.forEach(requestBuilder::setHeader);
        HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Set<Integer> retryableCodes = Set.of(429,408,409,500,502,503,504);
        if (retryableCodes.contains(response.statusCode())) {
            int maxRetries = 3;
            int attempt = 0;
            while (attempt < maxRetries) {
                attempt++;
                long delay = 1000L;
                Optional<String> retryValue = response.headers().firstValue("Retry-After");
                if (retryValue.isPresent())
                    delay = Integer.parseInt(retryValue.get()) * 1000L;
                LOGGER.info(String.format("will attempt %d/%d retries after %dms (%dsec) to %s",
                        attempt, maxRetries, delay,delay/1000, URL));
                Thread.sleep(delay);
                response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 429) break;
            }
        }
        if (response.statusCode() != 200) {
            return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()), new ServiceException(
                    String.format("request to '%s' returned %s, response: %s",URL,response.statusCode(), response.body()))));
        }
        return Either.right(Jsoup.parse(response.body()));
    }


    public static Either<ServiceError,Document> fetchDocumentFromURL(String routesURL) {
        try {
            return getHTMLDocFromURL(routesURL, Map.of());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),e));
        }
    }

    public static Either<ServiceError,String> getResponseBodyAsString(String requestURL, List<String> headers) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).headers(headers.toArray(String[]::new)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()), new ServiceException(
                                String.format("request to '%s' returned %s, response: %s",requestURL,response.statusCode(), response.body()))));
            }
            return Either.right(response.body());
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    public static <T> Either<ServiceError, T> getRequestBody(String requestURL, Class<T> classType) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()), new ServiceException(
                                String.format("request to '%s' returned %s, response: %s",requestURL,response.statusCode(), response.body()))));
            }
            String jsonBody = response.body();
            return extractResponseClass(jsonBody, classType);
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    public static <T> Either<ServiceError, T> getRequestBody(String requestURL, List<String> headers, Class<T> classType) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).headers(headers.toArray(String[]::new)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()),
                        new ServiceException(String.format("request to '%s' returned %s, response: %s",
                                requestURL,response.statusCode(),response.body()))));
            }
            String jsonBody = response.body();
            return extractResponseClass(jsonBody, classType);
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    public static <T> Either<ServiceError,T> getRequestBody(String requestURL, TypeReference<T> typeReference) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.getStatusFromCode(response.statusCode()),
                        new ServiceException(String.format("request to '%s' returned %s, response: %s",
                                requestURL,response.statusCode(), response.body()))));
            }
            String jsonBody = response.body();
            return extractResponseClass(jsonBody, typeReference);
        } catch (URISyntaxException e) {
            String message = String.format("error creating uri: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (IOException | InterruptedException e) {
            String message = String.format("error sending request: %s",e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    private static <T> Either<ServiceError,T> extractResponseClass(String jsonBody, TypeReference<T> typeReference) {
        try {
            return Either.right(om.readValue(jsonBody,typeReference));
        } catch (JsonProcessingException e)  {
            String message = String.format("error reading json response body: %s to class: %s, error: %s",jsonBody,typeReference.getType().getTypeName(),e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    private static <T> Either<ServiceError,T> extractResponseClass(String jsonBody, Class<T> classType) {
        try {
            return Either.right(om.readValue(jsonBody,classType));
        } catch (JsonProcessingException e)  {
            String message = String.format("error reading json response body: %s to class: %s, error: %s",jsonBody,classType.getSimpleName(),e.getMessage());
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }
}
