package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;
import org.voyager.commons.model.location.Location;

class LocationTest {
    private Location location;

    @BeforeEach
    void init() {
        location = Location.builder().build();
    }
}