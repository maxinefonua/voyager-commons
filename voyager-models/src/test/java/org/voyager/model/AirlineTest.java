package org.voyager.model;

import org.junit.jupiter.api.Test;
import org.voyager.model.airline.Airline;

import static org.junit.jupiter.api.Assertions.*;

class AirlineTest {
    private static final String DELTA_PATH = "dl-dal";
    private static final String DELTA_DISPLAY = "Delta";

    @Test
    void testAirlineMethods() {
        Airline airline = Airline.DELTA;
        assertEquals(DELTA_PATH,airline.getPathVariableFR());
        assertEquals(DELTA_DISPLAY,airline.getDisplayText());
    }
}