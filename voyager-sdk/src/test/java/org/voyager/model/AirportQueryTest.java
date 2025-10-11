package org.voyager.model;

import org.junit.jupiter.api.Test;
import org.voyager.model.airport.AirportType;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirportQueryTest {

    @Test
    void resolveRequestURL() {
        AirportQuery airportQuery = AirportQuery.builder()
                .withTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).build();
        assertEquals("/airports?type=CIVIL,MILITARY",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder()
                .withCountryCode("us").build();
        assertEquals("/airports?countryCode=US",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder()
                .withAirline(Airline.AIRNZ).build();
        assertEquals("/airports?airline=AIRNZ",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder().withAirline(Airline.JAPAN)
                .withTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).withCountryCode("us").build();
        assertEquals("/airports?countryCode=US&airline=JAPAN&type=CIVIL,MILITARY",
                airportQuery.getRequestURL());
    }

    @Test
    void builder() {
        // all null fields
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().build());
        // all valid
        AirportQuery airportQuery = AirportQuery.builder().withAirline(Airline.JAPAN)
                .withTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).withCountryCode("us").build();
        assertEquals(List.of(AirportType.CIVIL,AirportType.MILITARY),airportQuery.getAirportTypeList());
        assertEquals(Airline.JAPAN,airportQuery.getAirline());
        assertEquals("US",airportQuery.getCountryCode());
    }

    @Test
    void builderCountryCode() {
        // nonnull country code
        assertThrows(NullPointerException.class,()->AirportQuery.builder().withCountryCode(null).build());
        // invalid country code values
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().withCountryCode("").build());
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().withCountryCode("abs").build());
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().withCountryCode("34").build());
        // valid country code
        AirportQuery airportQuery = AirportQuery.builder().withCountryCode("to").build();
        assertEquals("TO",airportQuery.getCountryCode());
    }

    @Test
    void builderTypeList() {
        // nonnull type list
        assertThrows(NullPointerException.class,()->AirportQuery.builder().withTypeList(null).build());
        // invalid type list values
        List<AirportType> airportTypeList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().withTypeList(airportTypeList).build());
        airportTypeList.add(null);
        assertThrows(IllegalArgumentException.class,()->AirportQuery.builder().withTypeList(airportTypeList).build());
        // valid type list
        airportTypeList.remove(null);
        airportTypeList.add(AirportType.UNVERIFIED);
        AirportQuery airportQuery = AirportQuery.builder().withTypeList(airportTypeList).build();
        assertEquals(1,airportQuery.getAirportTypeList().size());
        assertEquals(AirportType.UNVERIFIED,airportQuery.getAirportTypeList().get(0));
    }

    @Test
    void builderAirline() {
        // nonnull builder methods
        assertThrows(NullPointerException.class,()->AirportQuery.builder().withAirline(null).build());
        // valid airline
        AirportQuery airportQuery = AirportQuery.builder().withAirline(Airline.DELTA).build();
        assertEquals(Airline.DELTA,airportQuery.getAirline());
    }
}