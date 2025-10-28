package org.voyager.commons.model.airport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportType;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AirportTest {
    private final String IATA = "ZZZ";
    private final String NAME = "name";
    private final String CITY = "city";
    private final String SUBDIVISON = "subdivision";
    private final String COUNTRY_CODE = "ZZ";
    private final Double LATITUDE = 1.0;
    private final Double LONGITUDE = 1.0;
    private final Double EXPECTED_DISTANCE_TO_ORIGIN = 157.24938127194397;
    private final AirportType AIRPORT_TYPE = AirportType.CIVIL;
    private final ZoneId ZONE_ID = ZoneId.of("Australia/Sydney");
    private static Airport airport;

    @BeforeEach
    public void setup() {
        airport = Airport.builder().iata(IATA).name(NAME).city(CITY).subdivision(SUBDIVISON)
                .countryCode(COUNTRY_CODE).latitude(LATITUDE).longitude(LONGITUDE).type(AIRPORT_TYPE)
                .zoneId(ZONE_ID).build();
    }

    @Test
    @DisplayName("test default builder")
    void testBuilder() {
        assertNotNull(airport);
        assertEquals(IATA,airport.getIata());
        assertEquals(NAME,airport.getName());
        assertEquals(CITY,airport.getCity());
        assertEquals(SUBDIVISON,airport.getSubdivision());
        assertEquals(COUNTRY_CODE,airport.getCountryCode());
        assertEquals(LATITUDE,airport.getLatitude());
        assertEquals(LONGITUDE,airport.getLongitude());
        assertEquals(AIRPORT_TYPE,airport.getType());
        assertEquals(ZONE_ID,airport.getZoneId());
        assertNull(airport.getDistance());
    }

    @Test
    void calculateDistanceKm() {
        Double distance = Airport.calculateDistanceKm(airport.getLatitude(),airport.getLongitude(),0.0,0.0);
        assertNotNull(distance);
        assertEquals(EXPECTED_DISTANCE_TO_ORIGIN,distance);
    }
}