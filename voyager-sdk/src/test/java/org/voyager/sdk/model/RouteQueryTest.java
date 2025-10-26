package org.voyager.sdk.model;

import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sdk.model.RouteQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteQueryTest {

    @Test
    void builderOrigin() {
        assertThrows(NullPointerException.class,()-> RouteQuery.builder().withOrigin(null).build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withOrigin("").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withOrigin("  ").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withOrigin("as2").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withOrigin("as").build());

        RouteQuery routeQuery = RouteQuery.builder().withOrigin("sjc").build();
        assertEquals("SJC",routeQuery.getOrigin());
        assertEquals("/routes?origin=SJC",routeQuery.getRequestURL());
    }

    @Test
    void builderDestination() {
        assertThrows(NullPointerException.class,()->RouteQuery.builder().withDestination(null).build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withDestination("").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withDestination("  ").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withDestination("as2").build());
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().withDestination("as").build());

        RouteQuery routeQuery = RouteQuery.builder().withDestination("sjc").build();
        assertEquals("SJC",routeQuery.getDestination());
        assertEquals("/routes?destination=SJC",routeQuery.getRequestURL());
    }

    @Test
    void getRequestURL() {
        assertThrows(IllegalArgumentException.class,()->RouteQuery.builder().build());

        RouteQuery routeQuery = RouteQuery.builder().withOrigin("SLC").withDestination("HEL")
                .withAirline(Airline.JAPAN).build();
        assertEquals("/routes?origin=SLC&destination=HEL&airline=JAPAN",routeQuery.getRequestURL());
    }

    @Test
    void builderAirline() {
        assertThrows(NullPointerException.class,()->RouteQuery.builder().withAirline(null).build());

        RouteQuery routeQuery = RouteQuery.builder().withAirline(Airline.JAPAN).build();
        assertEquals(Airline.JAPAN,routeQuery.getAirline());
        assertEquals("/routes?airline=JAPAN",routeQuery.getRequestURL());
    }
}