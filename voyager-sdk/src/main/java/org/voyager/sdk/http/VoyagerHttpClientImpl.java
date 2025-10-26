package org.voyager.sdk.http;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VoyagerHttpClientImpl implements VoyagerHttpClient {
    private final HttpClient httpClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(VoyagerHttpClientImpl.class);

    VoyagerHttpClientImpl() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    VoyagerHttpClientImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest httpRequest) {
        LOGGER.debug(String.format("get: %s",httpRequest.toString()));
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public Either<ServiceError, HttpResponse<String>> send(HttpRequest httpRequest) {
        LOGGER.debug(String.format("get: %s",httpRequest.toString()));
        try {
            return Either.right(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown while sending HttpRequest '%s'",httpRequest),
                    e));
        }
    }
}
