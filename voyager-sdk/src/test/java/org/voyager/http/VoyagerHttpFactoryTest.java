package org.voyager.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoyagerHttpFactoryTest {

    @Test
    void initialize() {
        assertThrows(IllegalStateException.class, VoyagerHttpFactoryTestImpl::getClient);
        assertDoesNotThrow(()->VoyagerHttpFactoryTestImpl.initialize("test-token"));
    }

    @Test
    void getClient() {
    }

    @Test
    void request() {
    }

    @Test
    void testRequest() {
    }
}