package org.voyager.sdk.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;

public interface ServiceUtils {
    void verifyHealth();
    <T> Either<ServiceError, T> fetch(String requestURL,HttpMethod httpMethod,Class<T> responseType);
    <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, TypeReference<T> typeReference);
    <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod, Class<T> responseType, Object requestBody);
    <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod, TypeReference<T> typeReference, Object requestBody);
    Either<ServiceError,Void> fetchNoResponseBody(String requestURL, HttpMethod httpMethod);
}
