package org.voyager.sdk.model;

import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sdk.model.FlightQuery;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightQueryTest {

    @Test
    void builderFlightNumber() {
        // fails on no fields set
        assertThrows(IllegalArgumentException.class,()-> FlightQuery.builder().build());
        // fails on null field set
        assertThrows(NullPointerException.class,()->FlightQuery.builder().withFlightNumber(null).build());
        // fails on empty string
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().withFlightNumber("").build());
        // fails on blank string
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().withFlightNumber("  ").build());

        // valid field
        FlightQuery flightQuery = FlightQuery.builder().withFlightNumber("TS123").build();
        assertEquals("TS123",flightQuery.getFlightNumber());
        assertEquals("/flights?flightNumber=TS123",flightQuery.getRequestURL());
    }

    @Test
    void builderIsActive() {
        // fails on no fields set
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().build());
        // fails on null field set
        assertThrows(NullPointerException.class,()->FlightQuery.builder().withActive(null).build());

        // valid field
        FlightQuery flightQuery = FlightQuery.builder().withActive(true).build();
        assertEquals(true,flightQuery.getIsActive());
        assertEquals("/flights?isActive=true",flightQuery.getRequestURL());
    }

    @Test
    void builderAirline() {
        // fails on no fields set
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().build());
        // fails on null field set
        assertThrows(NullPointerException.class,()->FlightQuery.builder().withAirline(null).build());

        // valid field
        FlightQuery flightQuery = FlightQuery.builder().withAirline(Airline.AIRCHINA).build();
        assertEquals(Airline.AIRCHINA,flightQuery.getAirline());
        assertEquals("/flights?airline=AIRCHINA",flightQuery.getRequestURL());
    }

    @Test
    void builderRouteIdList() {
        // fails on no fields set
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().build());
        // fails on null field set
        assertThrows(NullPointerException.class,()->FlightQuery.builder().withRouteIdList(null).build());
        // fails on empty list
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().withRouteIdList(List.of()).build());
        // fails on null element
        List<Integer> routeIdList = new ArrayList<>();
        routeIdList.add(null);
        assertThrows(IllegalArgumentException.class,()->FlightQuery.builder().withRouteIdList(routeIdList).build());

        // valid field
        routeIdList.remove(null);
        routeIdList.add(999);
        routeIdList.add(888);
        FlightQuery flightQuery = FlightQuery.builder().withRouteIdList(routeIdList).build();
        assertEquals(888,flightQuery.getRouteIdList().get(1));
        assertEquals("/flights?routeId=999,888",flightQuery.getRequestURL());
    }

    @Test
    void resolveRequestURL() {
        FlightQuery flightQuery = FlightQuery.builder().withAirline(Airline.SOUTHWEST).withFlightNumber("TS99")
                .withActive(false).withRouteIdList(List.of(123,456,789)).build();
        assertEquals("/flights?routeId=123,456,789&flightNumber=TS99&airline=SOUTHWEST&isActive=false",
                flightQuery.getRequestURL());
    }
}