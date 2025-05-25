package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.voyager.error.HttpException;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.airport.Airport;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.voyager.constants.MessageConstants.*;

public class ServiceUtils {
    private static final ObjectMapper om = new ObjectMapper();

    static <T> Either<ServiceError,T> responseBody(@NonNull HttpResponse<String> response,
                                                          @NonNull Class<T> valueType,
                                                          @NonNull String requestURL) {
        if (response.statusCode() != 200) return Either.left(buildServiceError(response,requestURL));
        try {
            return Either.right(om.readValue(response.body(),valueType));
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, getJsonParseResponseBodyExceptionMessage(requestURL,valueType,response),e));
        }
    }

    static <T> Either<ServiceError,T> responseBodyList(@NonNull HttpResponse<String> response,
                                                       @NonNull TypeReference<T> typeReference,
                                                       @NonNull String requestURL) {
        if (response.statusCode() != 200) return Either.left(buildServiceError(response,requestURL));
        try {
            return Either.right(om.readValue(response.body(), typeReference));
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, getJsonParseResponseBodyExceptionMessage(requestURL,typeReference.getClass(),response),e));
        }
    }


    private static ServiceError buildServiceError(HttpResponse<String> response, String requestURL) {
        if (isNotBlank(response.body())) {
            try {
                HttpException exception = om.readValue(response.body(), HttpException.class);
                return new ServiceError(response.statusCode(),exception);
            } catch (JsonProcessingException e) { // TODO: implement alert for exceptions exposed via API
                return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                        getJsonParseResponseExceptionMessage(requestURL,response), e);
            }
        }
        return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceException.builder().message(getServiceExceptionBlankResponseBody(requestURL,response)).build()
        );
    }
}
