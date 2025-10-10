package org.voyager.http;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;

public class MockHttpRequest extends HttpRequest {
    public enum HttpType {
        FETCH_COUNTRY,
        FETCH_ROUTES,
        FETCH_ERROR
    }

    @Getter @Setter
    private HttpType requestType;

    @Override
    public String toString() {
        return "MockHttpRequest";
    }

    @Override
    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.empty();
    }

    @Override
    public String method() {
        return "";
    }

    @Override
    public Optional<Duration> timeout() {
        return Optional.empty();
    }

    @Override
    public boolean expectContinue() {
        return false;
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public Optional<HttpClient.Version> version() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }
}
