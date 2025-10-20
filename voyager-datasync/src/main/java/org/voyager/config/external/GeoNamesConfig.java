package org.voyager.config.external;

import lombok.Getter;

public class GeoNamesConfig {
    private final String baseURL = "https://secure.geonames.org";

    @Getter
    private final String nearbyPlacePathWithParams = baseURL.concat("/findNearbyPlaceNameJSON?username=%s&lat=%f&lng=%f");

    @Getter
    private final String timezonePathWithParams = baseURL.concat("/timezoneJSON?lat=%f&lng=%f&username=%s");

    @Getter
    private final String countryPathWithParams = baseURL.concat("/countryInfoJSON?username=%s");

    @Getter
    private final String getPathWithParams = baseURL.concat("/getJSON?geonameId=%d&username=%s");
}
