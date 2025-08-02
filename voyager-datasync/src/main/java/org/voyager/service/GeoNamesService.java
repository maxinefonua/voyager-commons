package org.voyager.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.geoname.*;
import org.voyager.utils.HttpRequestUtils;
import java.util.List;

public class GeoNamesService {
    private static final String baseURL = "https://secure.geonames.org";
    private static final String USERNAME = System.getenv("GEONAMES_USERNAME");

    private static final String nearbyPlacePath = "/findNearbyPlaceNameJSON";
    private static final String nearbyPlaceParams = "?username=%s&lat=%f&lng=%f";

    private static final String timezonePath = "/timezoneJSON";
    private static final String timezoneParams = "?lat=%f&lng=%f&username=%s";

    private static final String countryPath = "/countryInfoJSON";
    private static final String countryParams = "?username=%s";

    private static final String getPath = "/getJSON";
    private static final String getParams = "?geonameId=%d&username=%s";

    public static Either<ServiceError, List<GeoName>> findNearbyPlaces(@NonNull Double latitude, @NonNull Double longitude) {
        String requestURL = baseURL.concat(nearbyPlacePath).concat(String.format(nearbyPlaceParams,USERNAME,latitude,longitude));
        return HttpRequestUtils.getRequestBody(requestURL,GeoNameResponse.class).map(GeoNameResponse::getGeonames);
    }

    public static Either<ServiceError, Timezone> getTimezone(@NonNull Double latitude, @NonNull Double longitude) {
        String requestURL = baseURL.concat(timezonePath).concat(String.format(timezoneParams,latitude,longitude,USERNAME));
        return HttpRequestUtils.getRequestBody(requestURL,Timezone.class);
    }

    public static Either<ServiceError,GeoNameFull> fetchFull(@NonNull Long geonameId) {
        String requestURL = baseURL.concat(getPath).concat(String.format(getParams,geonameId,USERNAME));
        return HttpRequestUtils.getRequestBody(requestURL,GeoNameFull.class);
    }

    public static Either<ServiceError,List<CountryGN>> getCountryGNList() {
        String requestURL = baseURL.concat(countryPath).concat(String.format(countryParams,USERNAME));
        return HttpRequestUtils.getRequestBody(requestURL,CountryGNResponse.class).map(CountryGNResponse::getCountryGNList);
    }
}
