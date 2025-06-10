package org.voyager.error;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
class ServiceErrorTest {
    private static ServiceError serviceError;
    private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final HttpException HTTP_EXCEPTION = HttpException.builder().message("http message").build();

    @BeforeAll
    static void init() {
        serviceError = new ServiceError(HTTP_STATUS,HTTP_EXCEPTION);
    }

    @Test
    @DisplayName("valid constructors")
    void allConstructors() {
        assertEquals(HTTP_EXCEPTION,serviceError.getException());
        assertEquals(HTTP_STATUS,serviceError.getHttpStatus());
        assertEquals(HTTP_EXCEPTION.getMessage(),serviceError.getMessage());

        IllegalArgumentException exception = new IllegalArgumentException("Bad request exception message");
        ServiceError constructor2 = new ServiceError(400,exception);
        assertEquals(exception,constructor2.getException());
        assertEquals(HttpStatus.BAD_REQUEST,constructor2.getHttpStatus());
        assertEquals(exception.getMessage(),constructor2.getMessage());

        AuthenticationException authenticationException = new AuthenticationException("Unauthorized exception message");
        ServiceError constructor3 = new ServiceError(HttpStatus.UNAUTHORIZED,authenticationException);
        assertEquals(authenticationException,constructor3.getException());
        assertEquals(HttpStatus.UNAUTHORIZED,constructor3.getHttpStatus());
        assertEquals(constructor3.getMessage(),constructor3.getMessage());
    }
}