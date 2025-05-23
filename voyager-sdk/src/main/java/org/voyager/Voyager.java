package org.voyager;

import org.voyager.config.VoyagerConfig;
import org.voyager.http.VoyagerHttpFactory;
import org.voyager.service.airport.AirportService;

public class Voyager {
    private final VoyagerConfig voyagerConfig;
    private AirportService airportService;
    private final VoyagerHttpFactory voyagerHttpFactory;

    public Voyager(VoyagerConfig voyagerConfig) {
        this.voyagerConfig = voyagerConfig;
        this.voyagerHttpFactory = new VoyagerHttpFactory(voyagerConfig.getMaxThreads(), voyagerConfig.getAuthorizationToken());
    }

    public AirportService getAirportService() {
        if (airportService == null) airportService = new AirportService(voyagerConfig, voyagerHttpFactory);
        return airportService;
    }
}
