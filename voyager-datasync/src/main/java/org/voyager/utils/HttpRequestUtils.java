package org.voyager.utils;

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
}
