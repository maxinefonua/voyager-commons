package org.voyager.utils;

public class MessageUtils {
    private static final String EMPTY_ENV_VAR = "Environment variable %s cannot be empty.";
    public static String getEmptyEnvVarMessage(String envVarKey){
        return String.format(EMPTY_ENV_VAR,envVarKey);
    }

    private static final String INVALID_API_KEY = "Invalid API Key";
    public static String getInvalidApiKeyMessage() {
        return INVALID_API_KEY;
    }

    private static final String ILLEGAL_ACCESS_VALIDATOR = "IllegalAccessException thrown in NonNullFieldValidator accessing field '%s' of '%s'";
    public static String getIllegalAccessValidatorMessage(String fieldName, String objectClassName) {
        return String.format(ILLEGAL_ACCESS_VALIDATOR,fieldName,objectClassName);
    }
}
