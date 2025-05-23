package org.voyager.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static org.voyager.utils.ConstantsUtils.AUTH_TOKEN_HEADER_NAME;

public class VoyagerHttpClientFactory {
    private VoyagerHttpClient client;
    private final int maxConcurrentRequests;
    private final String authorizationToken;

    public VoyagerHttpClientFactory(int maxConcurrentRequests,String authorizationToken) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.authorizationToken = authorizationToken;
    }

    public VoyagerHttpClient getClient() {
        if (client == null) this.client = createClient();
        return this.client;
    }

    public HttpRequest buildRequest(URI uri,) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(AUTH_TOKEN_HEADER_NAME,authorizationToken)
                .build();
    }

    private VoyagerHttpClient createClient() {
        return new VoyagerHttpClient(maxConcurrentRequests);
    }
}