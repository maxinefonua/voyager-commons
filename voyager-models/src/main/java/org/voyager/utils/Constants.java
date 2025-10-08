package org.voyager.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class Constants {
    public static class Voyager {
        public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
        public static final String VOYAGER_BASE_URL = "VOYAGER_BASE_URL";

        public static class Headers {
            public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
            public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
            public static final String JSON_TYPE_VALUE = "application/json";
        }

        public static class ParameterNames {
            public static final String QUERY_PARAM_NAME = "q";
            public static final String SOURCE_PARAM_NAME = "source";
            public static final String SKIP_ROW_PARAM_NAME = "skipRowCount";
            public static final String LATITUDE_PARAM_NAME = "latitude";
            public static final String LONGITUDE_PARAM_NAME = "longitude";
            public static final String LIMIT_PARAM_NAME = "limit";
            public static final String CONTINENT_PARAM_NAME = "continent";
            public static final String COUNTRY_CODE_PARAM_NAME = "countryCode";
            public static final String LANGUAGE_ISO6391_PARAM_NAME = "iso6391";
            public static final String LANGUAGE_ISO6392_PARAM_NAME = "iso6392";
            public static final String LANGUAGE_ISO6393_PARAM_NAME = "iso6393";
            public static final String CURRENCY_CODE_PARAM_NAME = "currencyCode";
            public static final String TYPE_PARAM_NAME = "type";
            public static final String AIRLINE_PARAM_NAME = "airline";
            public static final String ROUTE_ID_PARAM_NAME = "routeId";
            public static final String IATA_PARAM_NAME = "iata";
            public static final String ID_PATH_VAR_NAME = "id";
            public static final String FLIGHT_NUMBER_PARAM_NAME = "flightNumber";
            public static final String ORIGIN_PARAM_NAME = "origin";
            public static final String DESTINATION_PARAM_NAME = "destination";
            public static final String EXCLUDE_PARAM_NAME = "exclude";
            public static final String EXCLUDE_ROUTE_PARAM_NAME = "excludeRoute";
            public static final String EXCLUDE_FLIGHT_PARAM_NAME = "excludeFlight";
            public static final String IS_ACTIVE_PARAM_NAME = "isActive";
            public static final String LOCATION_STATUS_PARAM_NAME = "status";
            public static final String SOURCE_PROPERTY_NAME = SOURCE_PARAM_NAME;
            public static final String SOURCE_ID_PARAM_NAME = "sourceId";
            public static final String AIRPORTS_PROPERTY_NAME = "airports";
        }

        public static class Path {
            public static final String IATA = "/iata";
            public static final String AIRLINES = "/airport-airlines";
            public static final String AIRPORTS = "/airports";
            public static final String NEARBY_AIRPORTS = "/nearby-airports";
            public static final String COUNTRIES = "/countries";
            public static final String FLIGHTS = "/flights";
            public static final String FLIGHT = "/flight";
            public static final String LOCATIONS = "/locations";
            public static final String LOCATION = "/location";
            public static final String PATH_AIRLINE = "/path-airline";
            public static final String PATH = "/path";
            public static final String ROUTES = "/routes";
            public static final String ROUTE = "/route";
            public static final String SEARCH_PATH = "/search";
            public static final String ATTRIBUTION_PATH = "/search-attribution";
            // TODO: make sub calls /search/fetch, /search/attribution
            public static final String FETCH_PATH = "/fetch";
        }

        public static class Regex {
            public static final String ALPHA2_CODE_REGEX = "^[a-zA-Z]{2}$";
            public static final String ALPHA3_CODE_REGEX = "^[a-zA-Z]{3}$";
            public static final String ALPHA3_CODE_REGEX_OR_EMPTY = "^([a-zA-Z]{3}|)$";
            public static final String ALPHA2_CODE_REGEX_OR_EMPTY = "^([a-zA-Z]{2}|)$";
            public static final String ENGLISH_APLHA_REGEX = "[A-Za-z]*";
        }
        public static class ConstraintMessage {
            public static final String LANGUAGE_ISO639_1_CONSTRAINT = "Must be a valid two-letter ISO 639-1 alpha-2 language code";
            public static final String LANGUAGE_ISO639_2_CONSTRAINT = "Must be a valid three-letter ISO 639-2 alpha-3 language code";
            public static final String LANGUAGE_ISO639_3_CONSTRAINT = "Must be a valid three-letter ISO 639-3 alpha-3 language code";
            public static final String IATA_CODE = "must be a valid three-letter ISO 3166-1 alpha-3 IATA code";
            public static final String COUNTRY_CODE = "must be a valid two-letter ISO 3166-1 alpha-2 IATA code";
        }
    }

    public static class GeoNames {
        public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    }

    private static final String ENV_VAR_LITERAL = "${%s}";

    public static void validateSystemProperty(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = System.getProperty(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(getUndefiniedSystemPropertyMessage(key));
        }
    }

    private static final String UNDEFINED_SYSTEM_PROP = "System property '%s' must be defined.";
    public static String getUndefiniedSystemPropertyMessage(String envVarKey){
        return String.format(UNDEFINED_SYSTEM_PROP,envVarKey);
    }
}
