package org.voyager.commons.model.airport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AirportPatchTest {
    private static final String NAME = "name";
    private static final String CITY = "city";
    private static final String SUBDIVISION = "subdivision";
    private static final AirportType TYPE = AirportType.MILITARY;

    private static AirportPatch airportPatch;

    @BeforeEach
    void setUp() {
        airportPatch = AirportPatch.builder().name(NAME).city(CITY).subdivision(SUBDIVISION)
                .type(TYPE.name()).build();
    }

    @Test
    void builder() {
        assertNotNull(airportPatch);
        assertEquals(NAME,airportPatch.getName());
        assertEquals(CITY,airportPatch.getCity());
        assertEquals(SUBDIVISION,airportPatch.getSubdivision());
        assertEquals(TYPE.name(),airportPatch.getType());
    }
}