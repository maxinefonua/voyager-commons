package org.voyager.sdk.http;

import org.junit.platform.commons.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class VoyagerHttpFactoryTestImpl extends VoyagerHttpFactory {
    private static final String BASE_URL = "http://test.org";

    protected static VoyagerHttpClient createClient() {
        return new VoyagerHttpClientImpl();
    }

    public static HttpRequest request(URI uri, HttpMethod httpMethod) {
        try {
            if (StringUtils.isBlank(uri.getScheme())) {
                String uriString = BASE_URL.concat(uri.toString());
                uri = new URI(uriString);
            }
            return MockHttpRequest.newBuilder()
                    .uri(uri)
                    .method(httpMethod.name(),HttpRequest.BodyPublishers.noBody())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
