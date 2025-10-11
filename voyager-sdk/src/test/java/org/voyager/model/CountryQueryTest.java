package org.voyager.model;

import org.junit.jupiter.api.Test;
import org.voyager.model.country.Continent;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CountryQueryTest {

    @Test
    void builder() {
        // fails on no fields set
        assertThrows(NullPointerException.class,()->CountryQuery.builder().build());
        // fails on null field set
        assertThrows(NullPointerException.class,()->CountryQuery.builder().withContinentList(null).build());
        // fails on empty list
        List<Continent> continentList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,()->CountryQuery.builder().withContinentList(continentList).build());
        // fails on null element
        continentList.add(null);
        assertThrows(IllegalArgumentException.class,()->CountryQuery.builder().withContinentList(continentList).build());
        continentList.remove(null);

        // valid field
        continentList.add(Continent.OC);
        continentList.add(Continent.AS);
        CountryQuery countryQuery = CountryQuery.builder().withContinentList(continentList).build();
        assertEquals(Continent.AS,countryQuery.getContinentList().get(1));
        assertEquals("/countries?countryCode=OC,AS",countryQuery.getRequestURL());
    }
}