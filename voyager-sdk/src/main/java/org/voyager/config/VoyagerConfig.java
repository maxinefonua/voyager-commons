package org.voyager.config;

import lombok.*;

import java.net.MalformedURLException;
import java.net.URL;

public class VoyagerConfig {
    private static final String AIRPORTS_PATH = "/airports";
    private static final String NEARBY_PATH = "/nearby-airports";
    private static final String ROUTES_PATH = "/routes";
    private static final String PATH_PATH = "/path";
    private static final String SEARCH_PATH = "/search";
    private static final String ATTRIBUTION_PATH = "/search-attribution";
    private static final String LOCATIONS_PATH = "/locations";
    private static final String FLIGHTS_PATH = "/flights";

    @Getter
    private final int maxThreads;
    @Getter
    private final String baseURL;
    @Getter
    private final String authorizationToken;

    public VoyagerConfig(@NonNull Protocol protocol, @NonNull String host, int port, int maxThreads, @NonNull String authorizationToken) {
        this.maxThreads = maxThreads;
        this.baseURL = buildBaseURL(protocol.getValue(),host,port);
        this.authorizationToken = authorizationToken;
    }

    public String getAirportsServicePath() {
        return baseURL.concat(AIRPORTS_PATH);
    }

    public String getRoutesServicePath() {
        return baseURL.concat(ROUTES_PATH);
    }

    public String getPath() {
        return baseURL.concat(PATH_PATH);
    }

    public String getSearchPath() {
        return baseURL.concat(SEARCH_PATH);
    }

    public String getAttributionPath() {
        return baseURL.concat(ATTRIBUTION_PATH);
    }

    public String getLocationsPath() {
        return baseURL.concat(LOCATIONS_PATH);
    }

    public String getFlightsPath() {
        return baseURL.concat(FLIGHTS_PATH);
    }

    public String getNearbyPath() {
        return baseURL.concat(NEARBY_PATH);
    }

    private String buildBaseURL(String protocol, String host, int port) {
        try {
            return new URL(protocol,host,port,"").toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
