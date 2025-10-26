package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.location.LocationForm;
import org.voyager.commons.model.location.Source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocationFormTest {
    private LocationForm locationForm;

    @BeforeEach
    void init() {
        locationForm = LocationForm.builder().build();
    }

    @Test
    @DisplayName("default source")
    void getSource() {
        assertNotNull(locationForm.getSource());
        assertEquals(Source.MANUAL.toString(),locationForm.getSource());
    }

    @Test
    @DisplayName("default airports")
    void defaultAirports() {
        LocationForm defaultLocation = new LocationForm();
        assertNotNull(defaultLocation.getAirports());
        assertNotNull(defaultLocation.getAirports());
    }
}