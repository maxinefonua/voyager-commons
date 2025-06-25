package org.voyager.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpRequestUtils {
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtils.class);

    private static Document getHTMLDocFromURL(String URL, Map<String,String> headers) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(URL))
                .GET();
        headers.forEach(requestBuilder::setHeader);
        HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RuntimeException(String.format("Non-200 status returned from endpoint '%s'\nDeltaStatus: %d\nResponse: %s",URL,response.statusCode(),response.body()));
        return Jsoup.parse(response.body());
    }

    public static Document fetchDocumentFromURL(String routesURL) {
        try {
            return HttpRequestUtils.getHTMLDocFromURL(routesURL, Map.of());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Error while getting response from URL '%s'. Message: %s",routesURL,e.getMessage()),e);
            throw new RuntimeException(e);
        }
    }
}
