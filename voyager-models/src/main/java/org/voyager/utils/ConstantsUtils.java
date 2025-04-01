package org.voyager.utils;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

public class ConstantsUtils {
    public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
    private static final String ENV_VAR_LITERAL = "${%s}";

    public static final String QUERY_PARAM = "q";
    public static final String SKIP_ROW_PARAM = "skipRowCount";
    public static final String LATITUDE_PARAM = "latitude";
    public static final String LONGITUDE_PARAM = "longitude";
    public static final String LIMIT_PARAM = "limit";

    public static void validateEnvironVars(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = System.getenv(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(MessageUtils.getEmptyEnvVarMessage(key));
        }
    }
}
