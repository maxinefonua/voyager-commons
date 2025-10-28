package org.voyager.sync.config.external;

import lombok.Getter;

public class FlightRadarConfig {
    private final String baseURL = "https://www.flightradar24.com";

    @Getter
    private final String authHeader = "authority";
    @Getter
    private final String authValue = "www.flightradar24.com";
    @Getter
    private final String routesPathWithParam = baseURL.concat("/data/airlines/%s/routes");
    @Getter
    private final String flightPathWithParam = baseURL.concat("/data/flights/%s");
    @Getter
    private final String airportPathWithParams = routesPathWithParam.concat("?get-airport-arr-dep=%s&format=json");
    @Getter
    private final String airportRoutesPathWithParam = baseURL.concat("/data/airports/%s/routes");
    @Getter
    private final String airportRoutesPathWithParams = airportRoutesPathWithParam.concat("?get-airport-arr-dep=%s&format=json");
    @Getter
    private final String airportDetailsWithParam = baseURL.concat("/airports/traffic-stats/?airport=%s");
}
