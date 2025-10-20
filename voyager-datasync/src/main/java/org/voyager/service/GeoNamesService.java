package org.voyager.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.external.GeoNamesConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.geoname.*;
import org.voyager.utils.HttpRequestUtils;
import java.util.List;

public class GeoNamesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesService.class);
    private static String USERNAME;
    private static GeoNamesConfig geoNamesConfig;

    private static void validateUsername() {
        if (StringUtils.isBlank(USERNAME)) {
            throw new RuntimeException("GeoNamesService not yet initialized. " +
                    "Call to initialize() first with a valid GeoNames username.");
        }
    }

    public static void initialize(String gnUsername) {
        USERNAME = gnUsername;
        geoNamesConfig = new GeoNamesConfig();
    }

    public static Either<ServiceError, List<GeoName>> findNearbyPlaces(@NonNull Double latitude,
                                                                       @NonNull Double longitude) {
        validateUsername();
        String requestURL = String.format(geoNamesConfig.getNearbyPlacePathWithParams(),USERNAME,latitude,longitude);
        LOGGER.debug(String.format("fetching nearby GeoName places at endpoint: %s",requestURL));
        return HttpRequestUtils.getRequestBody(requestURL,GeoNameResponse.class).map(GeoNameResponse::getGeonames);
    }

    public static Either<ServiceError, Timezone> getTimezone(@NonNull Double latitude, @NonNull Double longitude) {
        validateUsername();
        String requestURL = String.format(geoNamesConfig.getTimezonePathWithParams(),latitude,longitude,USERNAME);
        LOGGER.debug(String.format("fetching timezone at endpoint: %s",requestURL));
        return HttpRequestUtils.getRequestBody(requestURL,Timezone.class);
    }

    public static Either<ServiceError,GeoNameFull> fetchFull(@NonNull Long geonameId) {
        validateUsername();
        String requestURL = String.format(geoNamesConfig.getGetPathWithParams(),geonameId,USERNAME);
        LOGGER.debug(String.format("fetching full GeoName details at endpoint: %s",requestURL));
        return HttpRequestUtils.getRequestBody(requestURL,GeoNameFull.class);
    }

    public static Either<ServiceError,List<CountryGN>> getCountryGNList() {
        validateUsername();
        String requestURL = String.format(geoNamesConfig.getCountryPathWithParams(),USERNAME);
        LOGGER.debug(String.format("fetching GeoName country list at endpoint: %s",requestURL));
        return HttpRequestUtils.getRequestBody(requestURL,CountryGNResponse.class).map(CountryGNResponse::getCountryGNList);
    }
}
