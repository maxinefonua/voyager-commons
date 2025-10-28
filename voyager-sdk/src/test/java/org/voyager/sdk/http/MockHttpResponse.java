package org.voyager.sdk.http;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

public class MockHttpResponse implements HttpResponse<String> {
    private final String body;
    private final int statusCode;

    public MockHttpResponse(String body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.of(Map.of(), (name, value) -> true);
    }

    @Override
    public HttpRequest request() {
        return HttpRequest.newBuilder().uri(URI.create("http://test.com")).build();
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return URI.create("http://test.com");
    }

    @Override
    public java.net.http.HttpClient.Version version() {
        return java.net.http.HttpClient.Version.HTTP_1_1;
    }
}