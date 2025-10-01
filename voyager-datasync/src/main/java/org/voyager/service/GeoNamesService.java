package org.voyager.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.model.geoname.*;
import org.voyager.model.language.LanguageISO;
import org.voyager.utils.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeoNamesService {
    private static final String downloadBaseURL = "https://download.geonames.org/export/dump/";
    private static final String languagesEndpoint = "iso-languagecodes.txt";

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesService.class);

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

    public static Either<ServiceError,List<LanguageISO>> getLanguageISOList() {
        String requestURL = downloadBaseURL.concat(languagesEndpoint);
        try {
            URL url = new URL(requestURL);
            URLConnection connection = url.openConnection();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {

                reader.readLine(); // skip header line
                String line;

                List<LanguageISO> languages = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split("\\t");
                    LanguageISO lang = LanguageISO.builder()
                            .alpha639code3(StringUtils.isNotBlank(tokens[0]) ? tokens[0] : null)
                            .alpha639code2(StringUtils.isNotBlank(tokens[1]) ? tokens[1] : null)
                            .alpha639code1(StringUtils.isNotBlank(tokens[2]) ? tokens[2] : null)
                            .name(tokens[3])
                            .build();
                    languages.add(lang);
                }
                return Either.right(languages);
            }
        } catch (IOException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,e));
        }
    }
}
