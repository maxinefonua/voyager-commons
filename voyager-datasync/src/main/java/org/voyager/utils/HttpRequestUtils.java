package org.voyager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpRequestUtils {
    private static final ObjectMapper om = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    private static Either<ServiceError,Document> getHTMLDocFromURL(String URL, Map<String,String> headers) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET();
        headers.forEach(requestBuilder::setHeader);
        HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, new ServiceException(
                    String.format("Non-200 status returned from endpoint '%s'\nStatus Code: %d\nResponse: %s",
                            URL,response.statusCode(),
                            response.body()))));
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

    public static <T> Either<ServiceError,T> getRequestBody(String requestURL, TypeReference<T> typeReference) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(requestURL)).build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        new ServiceException(String.format("Non-200 response from requestURL %s, status: %d, body: %s",
                                requestURL,response.statusCode(),response.body()))));
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
}
