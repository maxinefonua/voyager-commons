package org.voyager.utls;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ConstantsUtil {
    public static final String GEONAMES_API_USERNAME = "GEONAMES_API_USERNAME";
    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
    public static final String VOYAGER_API_KEY = "VOYAGER_API_KEY";
    private static final String ENV_VAR_LITERAL = "${%s}";

    public static void validateEnvironVars(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = System.getenv(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(MessageUtil.getEmptyEnvVarMessage(key));
        }
    }
}
