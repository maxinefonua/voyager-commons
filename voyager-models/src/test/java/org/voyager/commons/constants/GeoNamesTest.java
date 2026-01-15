package org.voyager.commons.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoNamesTest {

    @Test
    void getSearchPath() {
        assertEquals(GeoNames.GEONAMES.concat(GeoNames.SEARCH),GeoNames.getSearchPath());
    }

    @Test
    void getNearbyPath() {
        assertEquals(GeoNames.GEONAMES.concat(GeoNames.NEARBY_PLACES),GeoNames.getNearbyPath());
    }

    @Test
    void getTimezonePath() {
        assertEquals(GeoNames.GEONAMES.concat(GeoNames.TIMEZONE),GeoNames.getTimezonePath());
    }

    @Test
    void getFetchPath() {
        assertEquals(GeoNames.GEONAMES.concat(GeoNames.FETCH),GeoNames.getFetchPath());
    }

    @Test
    void getCountriesPath() {
        assertEquals(GeoNames.GEONAMES.concat(GeoNames.COUNTRIES),GeoNames.getCountriesPath());
    }
}