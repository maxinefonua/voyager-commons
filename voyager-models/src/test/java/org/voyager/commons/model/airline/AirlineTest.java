package org.voyager.commons.model.airline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirlineTest {
    private static final String DELTA_PATH = "dl-dal";
    private static final String DELTA_DISPLAY = "Delta Air Lines";

    @Test
    void testAirlineMethods() {
        Airline airline = Airline.DELTA;
        assertEquals(DELTA_PATH,airline.getPathVariableFR());
        assertEquals(DELTA_DISPLAY,airline.getDisplayText());
    }

    @Test
    void testFromPathVariable() {
        Airline expected = Airline.AEGEAN;
        Airline airline = Airline.fromPathVariableFR(expected.getPathVariableFR());
        assertEquals(expected,airline);

        expected = Airline.EMIRATES;
        airline = Airline.fromDisplayText(expected.getDisplayText());
        assertEquals(expected,airline);

        airline = Airline.fromPathVariableFR("jl-jtl");
        assertEquals(Airline.JAPAN,airline);
    }

    @Test
    void testExceptionThrown() {
        assertThrows(IllegalArgumentException.class,()->Airline.fromPathVariableFR(null));
        assertThrows(IllegalArgumentException.class,()->Airline.fromPathVariableFR("invalid"));

        assertThrows(IllegalArgumentException.class,()->Airline.fromDisplayText(null));
        assertThrows(IllegalArgumentException.class,()->Airline.fromDisplayText("invalid"));
    }
}