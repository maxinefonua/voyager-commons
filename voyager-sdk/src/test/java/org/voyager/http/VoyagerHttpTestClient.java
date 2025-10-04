package org.voyager.http;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VoyagerHttpTestClient implements VoyagerHttpClient {
    @Override
    public CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest httpRequest) {
        return null;
    }

    @Override
    public Either<ServiceError, HttpResponse<String>> send(HttpRequest httpRequest) {
        return null;
    }
}
