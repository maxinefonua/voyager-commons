package org.voyager.error;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceErrorTest {
    private static ServiceError serviceError;
    private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final ServiceException SERVICE_EXCEPTION = ServiceException.builder().message("http message").build();

    @BeforeAll
    static void init() {
        serviceError = new ServiceError(HTTP_STATUS,SERVICE_EXCEPTION);
    }

    @Test
    @DisplayName("valid constructors")
    void allConstructors() {
        assertEquals(SERVICE_EXCEPTION,serviceError.getException());
        assertEquals(HTTP_STATUS,serviceError.getHttpStatus());
        assertEquals(SERVICE_EXCEPTION.getMessage(),serviceError.getMessage());

        String testMessage = "test message";
        ServiceError construct1 = new ServiceError(HTTP_STATUS,testMessage,SERVICE_EXCEPTION);
        assertEquals(SERVICE_EXCEPTION,construct1.getException());
        assertEquals(HTTP_STATUS,construct1.getHttpStatus());
        assertEquals(testMessage,construct1.getMessage());

        IllegalArgumentException exception = new IllegalArgumentException("Bad request exception message");
        ServiceError constructor2 = new ServiceError(400,exception);
        assertEquals(exception,constructor2.getException());
        assertEquals(HttpStatus.BAD_REQUEST,constructor2.getHttpStatus());
        assertEquals(exception.getMessage(),constructor2.getMessage());

        AuthenticationException authenticationException = new AuthenticationException("Unauthorized exception message");
        ServiceError constructor3 = new ServiceError(HttpStatus.UNAUTHORIZED,authenticationException);
        assertEquals(authenticationException,constructor3.getException());
        assertEquals(HttpStatus.UNAUTHORIZED,constructor3.getHttpStatus());
        assertEquals(authenticationException.getMessage(),constructor3.getMessage());
    }

    @Test
    @DisplayName("test constructors with null parameters")
    void testNullInConstructors() {
        assertThrows(NullPointerException.class,() -> new ServiceError(200,null));
        assertThrows(NullPointerException.class,() -> new ServiceError(HttpStatus.UNAUTHORIZED,null));
        assertThrows(NullPointerException.class,() -> new ServiceError(null,null));
        assertThrows(NullPointerException.class,() -> new ServiceError(null,null,null));
        assertThrows(NullPointerException.class,() -> new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,"test",null));
        assertThrows(NullPointerException.class,() -> new ServiceError(null,"test",null));
        assertThrows(NullPointerException.class,() -> new ServiceError(null,"test",new Exception()));
        assertThrows(NullPointerException.class,() -> new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,null,new Exception()));
    }
}