package org.voyager.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

import static org.voyager.utils.ConstantsUtils.*;

public class VoyagerHttpFactory {
    private VoyagerHttpClient client;
    private final int maxConcurrentRequests;
    private final String authorizationToken;

    public VoyagerHttpFactory(int maxConcurrentRequests, String authorizationToken) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.authorizationToken = authorizationToken;
    }

    public VoyagerHttpClient getClient() {
        if (client == null) this.client = createClient();
        return this.client;
    }

    public HttpRequest getRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(AUTH_TOKEN_HEADER_NAME,authorizationToken)
                .GET()
                .build();
    }

    public HttpRequest postRequest(URI uri,String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(AUTH_TOKEN_HEADER_NAME,authorizationToken,
                        CONTENT_TYPE_HEADER_NAME,JSON_TYPE_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    private VoyagerHttpClient createClient() {
        return new VoyagerHttpClient(maxConcurrentRequests);
    }
}