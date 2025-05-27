package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationFormTest {
    private LocationForm locationForm;

    @BeforeEach
    void init() {
        locationForm = LocationForm.builder().build();
    }

    @Test
    @DisplayName("default source")
    void setSource() {
        assertNull(locationForm.getSource());
        locationForm.setSource("");
        assertNotNull(locationForm.getSource());
        assertEquals(Source.MANUAL.name(),locationForm.getSource());
    }

    @Test
    @DisplayName("default airports")
    void defaultAirports() {
        LocationForm defaultLocation = new LocationForm();
        assertNotNull(defaultLocation.getAirports());
    }
}