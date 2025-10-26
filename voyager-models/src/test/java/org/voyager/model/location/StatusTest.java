package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.location.Status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatusTest {
    private static Status status;

    @BeforeEach
    void setup() {
        status = Status.SAVED;
    }

    @Test
    @DisplayName("test constructor")
    void testConstructor() {
        assertNotNull(status);
    }
}