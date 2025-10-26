package org.voyager.sdk.http;

import org.apache.commons.lang3.StringUtils;
import org.voyager.commons.constants.Headers;
import java.net.URI;
import java.net.http.HttpRequest;

public class VoyagerHttpFactory {
    private static VoyagerHttpClient client;
    private static boolean initialized = false;
    private static String authorizationToken;

    VoyagerHttpFactory(){}
    /**
     * Initialize the factory with required authorization token
     * Must be called before using any other methods
     */
    public static synchronized void initialize(String authToken) {
        if (initialized) {
            throw new IllegalStateException("VoyagerHttpFactory already initialized");
        }
        if (StringUtils.isBlank(authToken)) {
            throw new IllegalArgumentException("Authorization token cannot be null or empty");
        }
        authorizationToken = authToken;
        client = createClient();
        initialized = true;
    }

    public static synchronized VoyagerHttpClient getClient() {
        checkInitialized();
        return client;
    }

    public static synchronized HttpRequest request(URI uri, HttpMethod httpMethod) {
        checkInitialized();
        return HttpRequest.newBuilder()
                .uri(uri)
                .headers(Headers.AUTH_TOKEN_HEADER_NAME,authorizationToken)
                .method(httpMethod.name(),HttpRequest.BodyPublishers.noBody())
                .build();
    }

    public static synchronized HttpRequest request(URI uri,HttpMethod httpMethod,String jsonPayload) {
        checkInitialized();
        return HttpRequest.newBuilder()
                .uri(uri)
                .header(Headers.AUTH_TOKEN_HEADER_NAME,authorizationToken)
                .header(Headers.CONTENT_TYPE_HEADER_NAME, Headers.JSON_TYPE_VALUE)
                .method(httpMethod.name(),HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("VoyagerHttpFactory not initialized. Call initialize() first.");
        }
    }

    protected static VoyagerHttpClient createClient() {
        return new VoyagerHttpClientImpl();
    }

    public static synchronized void reset() {
        initialized = false;
    }
}