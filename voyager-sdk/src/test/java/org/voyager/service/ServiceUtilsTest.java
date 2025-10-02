package org.voyager.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportType;
import org.voyager.model.location.Location;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.voyager.constants.MessageConstants.*;

class ServiceUtilsTest {
    private static final String AIRPORT_JSON_BODY = "{\"iata\":\"ITM\",\"name\":\"Osaka International Airport\",\"city\":\"Osaka\",\"subdivision\":\"Hyogo\",\"countryCode\":\"JP\",\"latitude\":34.7855,\"longitude\":135.438,\"type\":\"CIVIL\"}";
    private static final Airport VALID_AIRPORT = Airport.builder().iata("ITM").name("Osaka International Airport").city("Osaka").subdivision("Hyogo").countryCode("JP").latitude(34.7855).longitude(135.438).type(AirportType.CIVIL).build();
    private static final String NOT_FOUND_JSON_BODY = "{\"timestamp\":\"2025-05-22T17:35:18.874+00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Resource not found for path variable 'iata' with value 'eee'. Information on given IATA code is currently unavailable\",\"path\":\"/airports/eee\"}";
    private static final String EXPOSED_EXCEPTION_BODY = "Request processing failed: org.springframework.orm.jpa.JpaSystemException: Identifier of entity 'org.voyager.model.entity.AirportEntity' must be manually assigned before calling 'persist()'\n";
    // TODO: use actual http response string example
    private static final String HTTP_RESPONSE_TO_STRING = "HttpResponse toString not yet implemented";
    private static final String REQUEST_URL = "testURL";

    @Mock
    private HttpResponse<String> httpResponse;
    @Mock
    private HttpResponse<String> httpResponseException;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        assertNotNull(httpResponse);
        when(httpResponse.body()).thenReturn(AIRPORT_JSON_BODY);
        assertEquals(AIRPORT_JSON_BODY,httpResponse.body());
        when(httpResponse.statusCode()).thenReturn(200);
        assertEquals(200,httpResponse.statusCode());
        when(httpResponse.toString()).thenReturn(HTTP_RESPONSE_TO_STRING);

        assertNotNull(httpResponseException);
        when(httpResponseException.body()).thenReturn(NOT_FOUND_JSON_BODY);
        assertEquals(NOT_FOUND_JSON_BODY,httpResponseException.body());
        when(httpResponseException.statusCode()).thenReturn(404);
        assertEquals(404,httpResponseException.statusCode());
        when(httpResponseException.toString()).thenReturn(HTTP_RESPONSE_TO_STRING);
    }

    @Test
    @DisplayName("valid response body")
    void extractMappedResponse() {
        Either<ServiceError,Airport> either = ServiceUtils.extractMappedResponse(httpResponse, Airport.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(VALID_AIRPORT,either.get());
    }

    @Test
    @DisplayName("valid exception response body")
    void exceptionExtractMappedResponse() {
        Either<ServiceError,Airport> either = ServiceUtils.extractMappedResponse(httpResponseException, Airport.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isLeft());
        String expectedMessage = "Resource not found for path variable 'iata' with value 'eee'. Information on given IATA code is currently unavailable";
        assertEquals(expectedMessage,either.getLeft().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,either.getLeft().getHttpStatus());
    }

    @Test
    @DisplayName("invalid exception response body")
    void exceptionExtractMappedResponseInvalid() {
        assertNotNull(httpResponseException);
        when(httpResponseException.body()).thenReturn(EXPOSED_EXCEPTION_BODY);
        assertEquals(EXPOSED_EXCEPTION_BODY,httpResponseException.body());
        when(httpResponseException.statusCode()).thenReturn(500);
        assertEquals(500,httpResponseException.statusCode());

        Either<ServiceError,Airport> either = ServiceUtils.extractMappedResponse(httpResponseException, Airport.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isLeft());
        String expectedMessage = getJsonParseResponseExceptionMessage(REQUEST_URL,httpResponseException);
        assertEquals(expectedMessage,either.getLeft().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,either.getLeft().getHttpStatus());
        assertEquals(JsonParseException.class,either.getLeft().getException().getClass());
    }

    @Test
    @DisplayName("blank exception response body")
    void exceptionExtractMappedResponseBlank() {
        assertNotNull(httpResponseException);
        when(httpResponseException.body()).thenReturn("");
        assertTrue(StringUtils.isBlank(httpResponseException.body()));
        when(httpResponseException.statusCode()).thenReturn(500);
        assertEquals(500,httpResponseException.statusCode());

        Either<ServiceError,Airport> either = ServiceUtils.extractMappedResponse(httpResponseException, Airport.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isLeft());
        String expectedMessage = getServiceExceptionBlankResponseBody(REQUEST_URL,httpResponseException);
        assertEquals(expectedMessage,either.getLeft().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,either.getLeft().getHttpStatus());
        assertEquals(ServiceException.class,either.getLeft().getException().getClass());
    }

    @Test
    @DisplayName("blank valid response body")
    void extractMappedResponseBlank() {
        assertNotNull(httpResponseException);
        when(httpResponseException.body()).thenReturn("");
        assertTrue(StringUtils.isBlank(httpResponseException.body()));
        when(httpResponseException.statusCode()).thenReturn(200);
        assertEquals(200,httpResponseException.statusCode());
        String validHttpResponseBlankBody = "Valid HttpResponse but with blank body";
        when(httpResponseException.toString()).thenReturn(validHttpResponseBlankBody);

        Either<ServiceError,Airport> either = ServiceUtils.extractMappedResponse(httpResponseException, Airport.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isLeft());
        String expectedMessage = getJsonParseResponseBodyExceptionMessage(REQUEST_URL,Airport.class,httpResponseException);
        assertEquals(expectedMessage,either.getLeft().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,either.getLeft().getHttpStatus());
        assertEquals(MismatchedInputException.class,either.getLeft().getException().getClass());
    }

    @Test
    @DisplayName("incorrect class for parsing")
    void extractMappedResponseIncorrectClass() {
        Either<ServiceError,Location> either = ServiceUtils.extractMappedResponse(httpResponse, Location.class,REQUEST_URL);
        assertNotNull(either);
        assertTrue(either.isLeft());
        String expectedMessage = String.format("Processing exception thrown while parsing response body from 'testURL'. Confirm [%s] is the correct class for this response: '%s'",
                Location.class.getName(),
                HTTP_RESPONSE_TO_STRING);
        assertEquals(expectedMessage,either.getLeft().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,either.getLeft().getHttpStatus());
        assertEquals(UnrecognizedPropertyException.class,either.getLeft().getException().getClass());
    }

    @Test
    @DisplayName("null args for responseBody")
    void extractMappedResponseNullArgs() {
        Class<Object> nullClass = null;
        assertThrows(NullPointerException.class,() -> ServiceUtils.extractMappedResponse(httpResponse, Airport.class,null));
        assertThrows(NullPointerException.class,() -> ServiceUtils.extractMappedResponse(httpResponse, nullClass, REQUEST_URL));
        assertThrows(NullPointerException.class,() -> ServiceUtils.extractMappedResponse(null, Airport.class,REQUEST_URL));
        assertThrows(NullPointerException.class,() -> ServiceUtils.extractMappedResponse(null, nullClass,null));
    }
}