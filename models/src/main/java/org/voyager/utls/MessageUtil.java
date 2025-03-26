package org.voyager.utls;

public class MessageUtil {
    private static final String EMPTY_ENV_VAR = "Environment variable %s cannot be empty.";
    public static String getEmptyEnvVarMessage(String envVarKey){
        return String.format(EMPTY_ENV_VAR,envVarKey);
    }

    private static final String INVALID_API_KEY = "Invalid API Key";
    public static String getInvalidApiKeyMessage() {
        return INVALID_API_KEY;
    }
}
