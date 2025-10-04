package org.voyager.config;

import lombok.*;

import java.net.MalformedURLException;
import java.net.URL;

public class VoyagerConfig {
    @Getter
    private final int maxThreads;
    @Getter
    private final String baseURL;
    @Getter
    private final String authorizationToken;

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, int port, int maxThreads,
                         @NonNull String authorizationToken) {
        this.maxThreads = maxThreads;
        this.baseURL = buildBaseURL(protocol.getValue(),host,port);
        this.authorizationToken = authorizationToken;
    }

    private String buildBaseURL(String protocol, String host, int port) {
        try {
            return new URL(protocol,host,port,"").toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
