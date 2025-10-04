package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;

class LocationTest {
    private Location location;

    @BeforeEach
    void init() {
        location = Location.builder().build();
    }
}