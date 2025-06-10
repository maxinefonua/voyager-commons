package org.voyager.http;

import lombok.NonNull;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.voyager.utils.ConstantsUtils.*;

public class VoyagerHttpFactory {
    private VoyagerHttpClient client;
    private final String authorizationToken;

    public VoyagerHttpFactory(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public VoyagerHttpClient getClient() {
        if (client == null) this.client = createClient();
        return this.client;
    }

    public HttpRequest request(URI uri,HttpMethod httpMethod) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(AUTH_TOKEN_HEADER_NAME,authorizationToken)
                .method(httpMethod.name(),HttpRequest.BodyPublishers.noBody())
                .build();
    }

    public HttpRequest request(URI uri,HttpMethod httpMethod,String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(AUTH_TOKEN_HEADER_NAME,authorizationToken,CONTENT_TYPE_HEADER_NAME,JSON_TYPE_VALUE)
                .method(httpMethod.name(),HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    private VoyagerHttpClient createClient() {
        return new VoyagerHttpClient();
    }
}