package org.voyager.commons.utils;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

public class Environment {
    private static final String ENV_VAR_LITERAL = "${%s}";
    private static final String UNDEFINED_SYSTEM_PROP = "Environment variable '%s' must be set.";

    public void validateEnvVars(List<String> envVarKeys) {
        for (String key : envVarKeys) {
            String envVarVal = getEnvVariable(key);
            if (StringUtils.isEmpty(envVarVal) || String.format(ENV_VAR_LITERAL,key).equals(envVarVal))
                throw new IllegalArgumentException(String.format(UNDEFINED_SYSTEM_PROP,key));
        }
    }

    protected String getEnvVariable(String key) {
        return System.getenv(key);
    }
}
