package org.voyager.sdk.http;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface VoyagerHttpClient {
    Either<ServiceError,HttpResponse<String>> send(HttpRequest httpRequest);
}
