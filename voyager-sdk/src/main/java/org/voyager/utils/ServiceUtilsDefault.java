package org.voyager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.constants.MessageConstants;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.error.ServiceHttpException;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ServiceUtilsDefault implements ServiceUtils {
    protected ServiceUtilsDefault(){}

    private static final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtilsDefault.class);

    @Override
    public <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, Class<T> responseType) {
        return fetchRequest(requestURL,httpMethod).flatMap(httpResponse ->
                ServiceUtilsDefault.extractMappedResponse(httpResponse,responseType,requestURL));
    }

    @Override
    public <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, TypeReference<T> typeReference) {
        return fetchRequest(requestURL,httpMethod).flatMap(httpResponse ->
                extractMappedResponse(httpResponse,typeReference,requestURL));
    }

    @Override
    public <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod, Class<T> responseType, Object requestBody) {
        try {
            URI uri = new URI(requestURL);
            String jsonPayload = om.writeValueAsString(requestBody);
            HttpRequest request = VoyagerHttpFactory.request(uri,httpMethod,jsonPayload);
            Either<ServiceError, HttpResponse<String>> responseEither = VoyagerHttpFactory.getClient().send(request);
            if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
            return ServiceUtilsDefault.extractMappedResponse(responseEither.get(),responseType,requestURL);
        } catch (URISyntaxException e) {
            String message = String.format("Exception thrown when building URI of %s",requestURL);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        } catch (JsonProcessingException e) {
            String message = String.format("Exception thrown when writing json payload of request body %s",requestBody);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    @Override
    public Either<ServiceError, Boolean> fetchNoResponseBody(String requestURL, HttpMethod httpMethod) {
        return fetchRequest(requestURL,httpMethod).flatMap(httpResponse ->
                confirmValidResponse(httpResponse,requestURL));
    }

    private static <T> Either<ServiceError,T> extractMappedResponse(@NonNull HttpResponse<String> response,
                                                                    @NonNull Class<T> returnType,
                                                                    @NonNull String requestURL) {
        if (response.statusCode() != 200) return Either.left(buildServiceError(response,requestURL));
        try {
            return Either.right(om.readValue(response.body(),returnType));
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.getJsonParseResponseBodyExceptionMessage(requestURL,returnType,response),e));
        }
    }

    private static Either<ServiceError,Boolean> confirmValidResponse(@NonNull HttpResponse<String> response,
                                                                     @NonNull String requestURL) {
        if (response.statusCode() != 204) return Either.left(buildServiceError(response,requestURL));
        return Either.right(true);
    }

    private static <T> Either<ServiceError,T> extractMappedResponse(@NonNull HttpResponse<String> response,
                                                                    @NonNull TypeReference<T> returnType,
                                                                    @NonNull String requestURL) {
        if (response.statusCode() != 200) return Either.left(buildServiceError(response,requestURL));
        try {
            return Either.right(om.readValue(response.body(), returnType));
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.getJsonParseResponseBodyExceptionMessage(requestURL,returnType.getClass(),response),e));
        }
    }

    private static ServiceError buildServiceError(HttpResponse<String> response, String requestURL) {
        if (isNotBlank(response.body())) {
            try {
                ServiceHttpException exception = om.readValue(response.body(), ServiceHttpException.class);
                return new ServiceError(response.statusCode(),exception);
            } catch (JsonProcessingException e) { // TODO: implement alert for exceptions exposed via API
                return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageConstants.getJsonParseResponseExceptionMessage(requestURL,response), e);
            }
        }
        return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceException.builder().message(
                        MessageConstants.getServiceExceptionBlankResponseBody(requestURL,response)).build()
        );
    }

    private static Either<ServiceError,HttpResponse<String>> fetchRequest(String requestURL, HttpMethod httpMethod) {
        try {
            URI uri = new URI(requestURL);
            HttpRequest request = getRequest(uri,httpMethod);
            return sendRequest(request);
        } catch (URISyntaxException e) {
            String message = String.format("Exception thrown when building URI of %s",requestURL);
            LOGGER.error(message);
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,message,e));
        }
    }

    protected static HttpRequest getRequest(URI uri, HttpMethod httpMethod) {
        return VoyagerHttpFactory.request(uri,httpMethod);
    }

    protected static Either<ServiceError,HttpResponse<String>> sendRequest(HttpRequest request) {
        return VoyagerHttpFactory.getClient().send(request);
    }
}
