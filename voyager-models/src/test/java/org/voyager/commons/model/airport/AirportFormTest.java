package org.voyager.commons.model.airport;

import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airport.AirportForm;

import static org.junit.jupiter.api.Assertions.*;
class AirportFormTest {

    @Test
    void getIata() {
        String iata = "test";
        AirportForm airportForm = AirportForm.builder().iata(iata).build();
        assertEquals(iata,airportForm.getIata());
    }

    @Test
    void getName() {
    }

    @Test
    void getCity() {
    }

    @Test
    void getSubdivision() {
    }

    @Test
    void getCountryCode() {
    }

    @Test
    void getLatitude() {
    }

    @Test
    void getLongitude() {
    }

    @Test
    void getAirportType() {
    }

    @Test
    void getZoneId() {
    }

    @Test
    void builder() {
    }

    @Test
    void toBuilder() {
    }
}