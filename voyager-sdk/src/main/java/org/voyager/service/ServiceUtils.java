package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.constants.MessageConstants;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;

import java.net.http.HttpResponse;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ServiceUtils {
    private static final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    static <T> Either<ServiceError,T> extractMappedResponse(@NonNull HttpResponse<String> response,
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

    static Either<ServiceError,Boolean> confirmValidResponse(@NonNull HttpResponse<String> response,
                                                             @NonNull String requestURL) {
        if (response.statusCode() != 204) return Either.left(buildServiceError(response,requestURL));
        return Either.right(true);
    }

    static <T> Either<ServiceError,T> extractMappedResponse(@NonNull HttpResponse<String> response,
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
            return new ServiceError(response.statusCode(),new ServiceException(response.body()));
        }
        return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceException.builder().message(
                        MessageConstants.getServiceExceptionBlankResponseBody(requestURL,response)).build()
        );
    }
}
