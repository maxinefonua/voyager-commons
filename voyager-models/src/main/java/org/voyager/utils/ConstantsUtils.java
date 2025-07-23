package org.voyager.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ConstantsUtils {
    public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String JSON_TYPE_VALUE = "application/json";
    public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
    public static final String VOYAGER_BASE_URL = "VOYAGER_BASE_URL";
    private static final String ENV_VAR_LITERAL = "${%s}";

    public static final String COUNTRY_CODE_REGEX = "^[a-zA-Z]{2}$";
    public static final String IATA_CODE_REGEX = "^[a-zA-Z]{3}$";

    public static final String QUERY_PARAM_NAME = "q";
    public static final String SOURCE_PARAM_NAME = "source";
    public static final String SKIP_ROW_PARAM_NAME = "skipRowCount";
    public static final String LATITUDE_PARAM_NAME = "latitude";
    public static final String LONGITUDE_PARAM_NAME = "longitude";
    public static final String LIMIT_PARAM_NAME = "limit";
    public static final String COUNTRY_CODE_PARAM_NAME = "countryCode";
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
    public static final String DELTA_STATUS_PARAM_NAME = "deltaStatus";
    public static final String LOCATION_STATUS_PARAM_NAME = "status";

    public static final String SOURCE_PROPERTY_NAME = SOURCE_PARAM_NAME;
    public static final String SOURCE_ID_PARAM_NAME = "sourceId";

    public static final String AIRPORTS_PROPERTY_NAME = "airports";

    public static final String ROUTES_PATH = "/routes";
    public static final String AIRPORTS_PATH = "/airports";
    public static final String IATA_PATH = "/iata";
    public static final String DELTA_PATH = "/delta";

    public static void validateEnvironVars(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = System.getenv(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(MessageUtils.getEmptyEnvVarMessage(key));
        }
    }
}
