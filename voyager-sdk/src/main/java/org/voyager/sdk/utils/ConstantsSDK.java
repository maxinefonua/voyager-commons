package org.voyager.sdk.utils;

import java.net.http.HttpResponse;

public class ConstantsSDK {
    private static final String JSON_PARSE_RESPONSE_BODY = "Processing exception thrown while parsing response body from '%s'. Confirm [%s] is the correct class for this response: '%s'";
    private static final String JSON_PARSE_RESPONSE_BODY_EXCEPTION = "Processing exception thrown while parsing http exception from '%s', with status code: %d. Alerting yet to be implemented for unwrapped exception '%s'";
    private static final String RESPONSE_BODY_EXCEPTION_BLANK = "Service exception with blank response body with status code '%d' returned from '%s'";

    public static  <T> String getJsonParseResponseBodyExceptionMessage(String requestURL, Class<T> classType, HttpResponse<String> response) {
        return String.format(JSON_PARSE_RESPONSE_BODY,requestURL,classType.getName(),response.body());
    }
    public static  <T> String getJsonParseResponseExceptionMessage(String requestURL, HttpResponse<String> response) {
        return String.format(JSON_PARSE_RESPONSE_BODY_EXCEPTION,requestURL,response.statusCode(),response.body());
    }
    public static  <T> String getServiceExceptionBlankResponseBody(String requestURL, HttpResponse<String> response) {
        return String.format(RESPONSE_BODY_EXCEPTION_BLANK,response.statusCode(),requestURL);
    }

}
