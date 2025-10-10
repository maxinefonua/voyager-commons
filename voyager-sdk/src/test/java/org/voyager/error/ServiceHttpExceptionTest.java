package org.voyager.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceHttpExceptionTest {

    @Test
    void getMessage() {
        ServiceHttpException exception = ServiceHttpException.builder().message("test message").build();
        assertEquals("test message",exception.getMessage());
    }
}