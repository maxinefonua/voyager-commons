package org.voyager.commons.model.geoname.query;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.ValidationUtils;

import static org.junit.jupiter.api.Assertions.*;

class GeoNearbyQueryTest {
    GeoNearbyQuery geoNearbyQuery;

    @Test
    void builder() {
        geoNearbyQuery = GeoNearbyQuery.builder().build();
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(geoNearbyQuery));
    }

    @Test
    void getAndSetLongLat() {
        geoNearbyQuery = GeoNearbyQuery.builder().latitude(-200.0).longitude(10.0).build();
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(geoNearbyQuery));
        geoNearbyQuery.setLatitude(-20.0);
        geoNearbyQuery.setLongitude(1000.0);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(geoNearbyQuery));
        geoNearbyQuery.setLongitude(100.0);
        assertDoesNotThrow(()->ValidationUtils.validateAndThrow(geoNearbyQuery));
    }

    @Test
    void getRadius() {
        geoNearbyQuery = GeoNearbyQuery.builder().longitude(10.0).latitude(10.0).radiusKm(0).build();
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(geoNearbyQuery));
        geoNearbyQuery.setRadiusKm(104);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(geoNearbyQuery));
        geoNearbyQuery.setRadiusKm(40);
        assertDoesNotThrow(()->ValidationUtils.validateAndThrow(geoNearbyQuery));
    }
}