package org.voyager.sdk.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceHttpExceptionTest {

    @Test
    void getMessage() {
        VoyagerServiceException exception = VoyagerServiceException.builder().message("test message").build();
        assertEquals("test message",exception.getMessage());
    }
}