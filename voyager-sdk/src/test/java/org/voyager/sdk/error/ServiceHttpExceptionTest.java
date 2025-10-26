package org.voyager.sdk.error;

import org.junit.jupiter.api.Test;
import org.voyager.sdk.error.ServiceHttpException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceHttpExceptionTest {

    @Test
    void getMessage() {
        ServiceHttpException exception = ServiceHttpException.builder().message("test message").build();
        assertEquals("test message",exception.getMessage());
    }
}