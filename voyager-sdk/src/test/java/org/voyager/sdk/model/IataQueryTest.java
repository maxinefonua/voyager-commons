package org.voyager.sdk.model;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IataQueryTest {

    @Test
    void builder() {
        assertThrows(IllegalArgumentException.class,()-> IataQuery.builder().build());
    }

    @Test
    void getAirlineList() {
        assertThrows(NullPointerException.class,()-> IataQuery.builder().withAirlineList(null).build());
    }

    @Test
    void getAirportTypeList() {
        assertThrows(NullPointerException.class,()-> IataQuery.builder().withAirportTypeList(null).build());
    }
}