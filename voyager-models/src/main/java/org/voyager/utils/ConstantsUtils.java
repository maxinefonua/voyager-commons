package org.voyager.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConstantsUtils {
    public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
    private static final String ENV_VAR_LITERAL = "${%s}";

    public static final String QUERY_PARAM_NAME = "q";
    public static final String SKIP_ROW_PARAM_NAME = "skipRowCount";
    public static final String LATITUDE_PARAM_NAME = "latitude";
    public static final String LONGITUDE_PARAM_NAME = "longitude";
    public static final String LIMIT_PARAM_NAME = "limit";
    public static final String COUNTRY_CODE_PARAM_NAME = "countryCode";
    public static final String TYPE_PARAM_NAME = "type";
    public static final String AIRLINE_PARAM_NAME = "airline";
    public static final String IATA_PARAM_NAME = "iata";

    public static void validateEnvironVars(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = System.getenv(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(MessageUtils.getEmptyEnvVarMessage(key));
        }
    }
}
