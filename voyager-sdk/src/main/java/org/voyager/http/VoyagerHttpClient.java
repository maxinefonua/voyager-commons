package org.voyager.http;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface VoyagerHttpClient {
    CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest httpRequest);
    Either<ServiceError,HttpResponse<String>> send(HttpRequest httpRequest);
}
