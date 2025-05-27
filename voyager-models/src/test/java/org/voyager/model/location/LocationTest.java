package org.voyager.model.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {
    private Location location;

    @BeforeEach
    void init() {
        location = Location.builder().build();
    }

    @Test
    @DisplayName("add airport duplicates")
    void addAirport() {
        assertNull(location.getAirports());
        String airportCode1 = "test1";
        location.addAirport(airportCode1);
        assertNotNull(location.getAirports());
        Set<String> expected = Set.of(airportCode1);
        assertEquals(expected, location.getAirports());

        // add duplicate
        location.addAirport(airportCode1);
        assertEquals(expected.size(), location.getAirports().size());

        // add new
        String airportCode2 = "test2";
        location.addAirport(airportCode2);
        expected = Set.of(airportCode1,airportCode2);
        assertEquals(expected.size(), location.getAirports().size());
        assertEquals(expected, location.getAirports());
    }

    @Test
    @DisplayName("removing airports")
    void removeAirport() {
        String airportCode1 = "test1";
        String airportCode2 = "test2";
        String airportCode3 = "test3";
        Set<String> expected = new HashSet<>(Set.of(airportCode1,airportCode2,airportCode3));
        location.setAirports(expected);
        assertEquals(expected, location.getAirports());

        // remove code
        location.removeAirport(airportCode2);
        expected = new HashSet<>(Set.of(airportCode1,airportCode3));
        assertEquals(expected.size(), location.getAirports().size());
        assertEquals(expected, location.getAirports());

        // remove code not in airports
        location.removeAirport(airportCode2);
        assertEquals(expected.size(), location.getAirports().size());

        // remove all codes in airports
        location.removeAirport(airportCode1);
        location.removeAirport(airportCode3);
        assertNotNull(location.getAirports());
        assertEquals(0, location.getAirports().size());
    }
}