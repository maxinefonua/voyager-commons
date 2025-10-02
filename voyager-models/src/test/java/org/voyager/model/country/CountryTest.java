package org.voyager.model.country;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryTest {

    @Test
    void builder() {
        String expectedCode = "ZZ";
        String exceptedCapital = "capital test";
        String expectedName = "test";
        Double[] expectedBounds = new Double[]{1.0,0.0,0.0,1.0};
        Long expectedPopulation = 1000000L;
        Double expectedArea = 25.5;
        Continent expectedContinent = Continent.OC;

        Country country = Country.builder().population(expectedPopulation).capitalCity(exceptedCapital)
                .areaInSqKm(expectedArea).continent(expectedContinent).name(expectedName)
                .code(expectedCode).bounds(expectedBounds).build();
        assertNotNull(country);
        assertEquals(expectedArea,country.getAreaInSqKm());
        assertEquals(expectedCode,country.getCode());
        assertEquals(exceptedCapital,country.getCapitalCity());
        assertEquals(expectedName,country.getName());
        assertEquals(expectedBounds,country.getBounds());
        assertEquals(expectedPopulation,country.getPopulation());
        assertEquals(expectedContinent,country.getContinent());
    }
}