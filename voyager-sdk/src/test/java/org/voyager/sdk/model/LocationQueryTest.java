package org.voyager.sdk.model;

import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.location.Source;
import org.voyager.commons.model.location.Status;
import org.voyager.sdk.model.LocationQuery;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationQueryTest {

    @Test
    void builderSource() {
        // set as null field
        assertThrows(NullPointerException.class,()-> LocationQuery.builder().withSource(null).build());

        // valid value
        LocationQuery locationQuery = LocationQuery.builder().withSource(Source.MANUAL).build();
        assertEquals(Source.MANUAL,locationQuery.getSource());
        assertEquals(Path.Admin.LOCATIONS.concat("?source=MANUAL"),locationQuery.getRequestURL());
    }

    @Test
    void builderLimit() {
        // set as null field
        assertThrows(NullPointerException.class,()-> LocationQuery.builder().withLimit(null).build());
        // set as less than minimum
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().withLimit(0).build());

        // valid value
        LocationQuery locationQuery = LocationQuery.builder().withLimit(20).build();
        assertEquals(20,locationQuery.getLimit());
        assertEquals(Path.Admin.LOCATIONS.concat("?limit=20"),locationQuery.getRequestURL());
    }

    @Test
    void builderStatusList() {
        // set as null field
        assertThrows(NullPointerException.class,()-> LocationQuery.builder().withStatusList(null).build());
        // set as empty list
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().withStatusList(List.of()).build());
        // set as list with null
        List<Status> statusList = new ArrayList<>();
        statusList.add(null);
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().withStatusList(statusList).build());

        // valid value
        statusList.remove(null);
        statusList.add(Status.ARCHIVED);
        statusList.add(Status.DELETE);
        LocationQuery locationQuery = LocationQuery.builder().withStatusList(statusList).build();
        assertEquals(Status.DELETE,locationQuery.getStatusList().get(1));
        assertEquals(Path.Admin.LOCATIONS.concat("?status=ARCHIVED,DELETE"),locationQuery.getRequestURL());
    }

    @Test
    void builderContinentList() {
        // set as null field
        assertThrows(NullPointerException.class,()-> LocationQuery.builder().withContinentList(null).build());
        // set as empty list
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().withContinentList(List.of()).build());
        // set as list with null
        List<Continent> continentList = new ArrayList<>();
        continentList.add(null);
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().withContinentList(continentList).build());

        // valid value
        continentList.remove(null);
        continentList.add(Continent.AN);
        continentList.add(Continent.AF);
        LocationQuery locationQuery = LocationQuery.builder().withContinentList(continentList).build();
        assertEquals(Continent.AN,locationQuery.getContinentList().get(0));
        assertEquals(Path.Admin.LOCATIONS.concat("?continent=AN,AF"),locationQuery.getRequestURL());
    }

    @Test
    void builderCountryCodeList() {
        // set as null field
        assertThrows(NullPointerException.class,()-> LocationQuery.builder().withCountryCodeList(null).build());
        // set as empty list
        assertThrows(IllegalArgumentException.class,()->
                LocationQuery.builder().withCountryCodeList(List.of()).build());

        // set as list with null
        List<String> countryCodeList = new ArrayList<>();
        countryCodeList.add(null);
        assertThrows(IllegalArgumentException.class,()->
                LocationQuery.builder().withCountryCodeList(countryCodeList).build());

        // set list with empty string
        countryCodeList.remove(null);
        countryCodeList.add("");
        assertThrows(IllegalArgumentException.class,()->
                LocationQuery.builder().withCountryCodeList(countryCodeList).build());

        // set list with malformatted string
        countryCodeList.remove("");
        countryCodeList.add("United States");
        assertThrows(IllegalArgumentException.class,()->
                LocationQuery.builder().withCountryCodeList(countryCodeList).build());

        // set list with valid strings
        countryCodeList.remove("United States");
        countryCodeList.add("us");
        countryCodeList.add("to");
        LocationQuery locationQuery = LocationQuery.builder().withCountryCodeList(countryCodeList).build();
        assertEquals("TO",locationQuery.getCountryCodeList().get(1));
        assertEquals(Path.Admin.LOCATIONS.concat("?countryCode=US,TO"),locationQuery.getRequestURL());
    }

    @Test
    void resolveRequestURL() {
        // fails on all fields null
        assertThrows(IllegalArgumentException.class,()-> LocationQuery.builder().build());
        LocationQuery locationQuery = LocationQuery.builder().withContinentList(List.of(Continent.AS,Continent.OC))
                .withStatusList(List.of(Status.SAVED,Status.NEW)).withCountryCodeList(List.of("to","jp"))
                .withLimit(50).withSource(Source.GEONAMES).build();
        assertEquals(Path.Admin.LOCATIONS.concat("?source=GEONAMES&limit=50&countryCode=TO,JP&status=SAVED,NEW&continent=AS,OC"),
                locationQuery.getRequestURL());
    }
}