package org.voyager.config;

import lombok.*;

import java.net.MalformedURLException;
import java.net.URL;

public class VoyagerConfig {
    private static final String AIRPORTS_PATH = "/airports";

    @Getter
    private final int maxThreads;
    @Getter
    private final String baseURL;
    @Getter
    private final String authorizationToken;

    public enum Protocol {
        HTTP("http");
        private String value;
        Protocol(String value) {
            this.value = value;
        }
        String getValue() {
            return this.value;
        }
    }

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, int port, int maxThreads, @NonNull String authorizationToken) {
        this.maxThreads = maxThreads;
        this.baseURL = buildBaseURL(protocol.value,host,port);
        this.authorizationToken = authorizationToken;
    }

    public String getAirportsServicePath() {
        return baseURL.concat(AIRPORTS_PATH);
    }

    private String buildBaseURL(String protocol, String host, int port) {
        try {
            return new URL(protocol,host,port,"").toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
