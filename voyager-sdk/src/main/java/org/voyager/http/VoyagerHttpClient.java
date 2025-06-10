package org.voyager.http;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class VoyagerHttpClient {
    private final HttpClient httpClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(VoyagerHttpClient.class);

    public VoyagerHttpClient() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    public CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest httpRequest) {
        LOGGER.debug(String.format("get: %s",httpRequest.toString()));
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    public Either<ServiceError,HttpResponse<String>> send(HttpRequest httpRequest) {
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
